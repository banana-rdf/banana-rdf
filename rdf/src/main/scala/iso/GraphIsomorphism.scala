package org.w3.banana.iso

import org.w3.banana.{ RDF, RDFOps }

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success, Try }

/**
 * Methods to establish Graph Equivalences
 *
 * Following Jeremy J. Carroll's "Matching RDF Graphs" article
 *  http://www.hpl.hp.com/techreports/2001/HPL-2001-293.pdf
 *
 *
 * Also I found it useful to think in terms of the RDF category
 * as defined by Benjamin Braatz in "Formal Modelling and Application of Graph Transformations
 * in the Resource Description Framework" Proposition 2.1 ( p 16 )
 *
 * http://www.researchgate.net/profile/Benjamin_Braatz/publication/40635984_Formal_Modelling_and_Application_of_Graph_Transformations_in_the_Resource_Description_Framework/file/d912f50d3189b51ef1.pdf
 *
 * This code can be ameliorated in a number of ways:
 *
 *  - by using lazy data structures
 *  - by optimising memory
 *
 *
 * @param mappingGen a mapping generator that knows to find for two graphs the possible mappings of bnodes between
 *                   them. Better mapping generators will find less mappings for the same graphs, without missing out
 *                   on correct ones.
 * @param ops RDFOPs
 * @tparam Rdf RDF implementation to work with
 */
class GraphIsomorphism[Rdf <: RDF](mappingGen: MappingGenerator[Rdf])(implicit ops: RDFOps[Rdf]) {

  import ops._

  /**
   * filter a graphs into two subgraphs the first one containing no blank nodes
   * the other one containing only statements with blank nodes
   *
   * @param graph
   * @return a pair of Ground graph and non ground graph
   */
  def groundTripleFilter(graph: Rdf#Graph): (Rdf#Graph, Rdf#Graph) = {
    var ground = Graph.empty
    var nonGround = Graph.empty
    for (triple <- graph.triples) {
      triple match {
        case Triple(s, r, o) if s.isBNode || o.isBNode => nonGround = nonGround + triple
        case _ => ground = ground + triple
      }
    }
    (ground, nonGround)
  }


  /**
   * Find possible bnode mappings
   * @param g1
   * @param g2
   * @return A list of possible bnode mappings or a reason for the error
   */
  def findPossibleMappings(g1: Rdf#Graph, g2: Rdf#Graph):
  Try[List[List[(Rdf#BNode, Rdf#BNode)]]] = {
    if (g1.size != g2.size)
      return Failure(MappingException(s"graphs don't have the same number of triples: g1.size=${g1.size} g2.size=${g2.size}"))

    val (grnd1, nongrnd1) = groundTripleFilter(g1)
    val (grnd2, nongrnd2) = groundTripleFilter(g2)
    if (grnd1.size != grnd2.size)
      return Failure(MappingException("the two graphs don't have the same number of ground triples. " +
        s"ground(g1).size=${grnd1.size} ground(g2).size=${grnd2.size}"))

    mappingGen.bnodeMappingGenerator(nongrnd1, nongrnd2) map { listMap =>
      val keys = listMap.keys.toList
      def tree(keys: List[Rdf#BNode]): List[List[(Rdf#BNode, Rdf#BNode)]] = {
        keys match {
          case head :: tail => listMap(keys.head).toList.flatMap { mappedBN =>
            tree(tail).map((head, mappedBN) :: _)
          }
          case Nil => List(List())
        }
      }
      tree(keys)
    }
  }

  def findAnswer(g1: Rdf#Graph, g2: Rdf#Graph): Try[List[(Rdf#BNode, Rdf#BNode)]] = {
    findPossibleMappings(g1, g2).flatMap { possibleAnswers =>
      val verifyAnswers = possibleAnswers.map(answer =>
        answer -> mapVerify(g1, g2, Map(answer: _*))
      )
      val answerOpt = verifyAnswers.find { case (_, err) => err == Nil }
      answerOpt.map(a => Success(a._1)).getOrElse(Failure(NoMappingException(verifyAnswers)))
    }
  }

  /**
   * Verify that the bnode bijection allows one to map graph1 to graph2
   * @param graph1
   * @param graph2
   * @param bnodeBijection  a bijection of bnodes. Each kay maps to one value and vice-versa
   * @return a list of exceptions in case of failure or an empty list in case of success
   *
   */
  def mapVerify(graph1: Rdf#Graph, graph2: Rdf#Graph, bnodeBijection: Map[Rdf#BNode, Rdf#BNode]): List[MappingException] = {
    def bnmap(node: Rdf#Node): Rdf#Node = node.fold(uri => uri, bnode => bnodeBijection(bnode), lit => lit)
    // verify that both graphs are the same size
    if (graph1.size != graph2.size)
      return List(MappingException(s"graphs not of same size. graph1.size=${graph1.size} graph2.size=${graph2.size}"))

    //1. verify that bnodeBijection is a bijection ( it may be nicer to have a data structure that guarantees it)
    if (bnodeBijection.map(_.swap).size != bnodeBijection.size)
      return List(MappingException(s"bnodeBijection is not a bijection: some keys map to more than one value"))

    try {
      for (Triple(sub, rel, obj) <- graph1.triples) {
        try {
          val mapped = makeTriple(bnmap(sub), rel, bnmap(obj))
          if (!graph2.contains(mapped)) throw MappingException(s"could not find map($sub,$rel,$obj)=$mapped in graph2")
        } catch {
          case e: java.util.NoSuchElementException => {
            throw MappingException(s"could not find map for $sub or $obj")
          }
        }
      }
    } catch {
      case e: MappingException => return List(e)
    }

    List() // no errors

    //do I have to test that the mapping goes the other way too? Or is it sufficient if the bnodesmap is a bijection?
  }

  case class NoMappingException(val reasons: List[(List[(Rdf#BNode, Rdf#BNode)], List[MappingError])]) extends MappingError {
    def msg = "No mapping found"
    override def toString() = s"NoMappingException($reasons)"
  }


}

trait MappingError extends NoStackTrace {
  def msg: String
}

case class MappingException(msg: String) extends MappingError {
  override def toString() = s"MappingException($msg)"
}

/**
 * Trait for implementations of BNode Mapping Generators.
 * A good implementation should never leave out a correct mapping.
 * Better implementations should return less mappings.
 * But better implementations will also tend to be less easy to understand.
 */
trait MappingGenerator[Rdf<:RDF] {
  /**
   * generate a list of possible bnode mappings, filtered by applying classification algorithm
   * on the nodes by relating them to other edges
   * @param g1 first graph
   * @param g2 second graph
   * @return a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which
   *         they should corresond to
   */
  def bnodeMappingGenerator(g1: Rdf#Graph, g2: Rdf#Graph): Try[ListMap[Rdf#BNode, mutable.Set[Rdf#BNode]]]
}

/*
 * The SimpleMappingGenerator implements only the first stage of Jeremy
 * Carroll's optimisation strategy. It classifies nodes only by the arrows
 * going in and out, but does not follow those further.
 */
class SimpleMappingGenerator[Rdf<:RDF](implicit ops: RDFOps[Rdf]) extends MappingGenerator[Rdf] {
  import ops._
  /**
   * generate a list of possible bnode mappings, filtered by applying classification algorithm
   * on the nodes by relating them to other edges
   * @param g1
   * @param g2
   * @return a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which
   *         they should corresond to
   */
  def bnodeMappingGenerator(g1: Rdf#Graph, g2: Rdf#Graph): Try[ListMap[Rdf#BNode, mutable.Set[Rdf#BNode]]] = Try {
    val clz1 = bnodeClassify(g1)
    val clz2 = bnodeClassify(g2)
    if (clz1.size != clz2.size)
      throw ClassificationException("the two graphs don't have the same number of classes.", clz1, clz2)
    val mappingOpts: mutable.Map[Rdf#BNode, mutable.Set[Rdf#BNode]] = mutable.HashMap[Rdf#BNode, mutable.Set[Rdf#BNode]]()
    for {
      (vt, bnds1) <- clz1 // .sortBy { case (vt, bn) => bn.size }
      bnds2 <- clz2.get(vt)
    } {
      if (bnds2.size != bnds1.size)
        throw ClassificationException(s"the two graphs don't have the same number of bindings for type $vt", clz1, clz2)
      for (bnd <- bnds1) {
        mappingOpts.get(bnd).orElse(Some(mutable.Set.empty[Rdf#BNode])).map { bnset =>
          mappingOpts.put(bnd, bnset ++= bnds2)
        }
      }
    }
    ListMap(mappingOpts.toList.sortBy(_._2.size): _*)
  }

  /**
   * This classification can be improved, but it is easier to debug while it is not so effective.
   * @param graph
   * @return a classification of bnodes by type, where nodes can only be matched by other nodes of the same type
   */
  def bnodeClassify(graph: Rdf#Graph): Map[VerticeType, Set[Rdf#BNode]] = {
    val bnodeClass = mutable.HashMap[Rdf#BNode, VerticeType]()
    for (Triple(subj, rel, obj) <- graph.triples) {
      if (subj.isBNode) {
        val bn = subj.asInstanceOf[Rdf#BNode]
        bnodeClass.get(bn) orElse {
          val vt = VerticeType()
          bnodeClass.put(bn, vt)
          Some(vt)
        } map { vt =>
          vt.setForwardRel(rel, obj)
        }
      }
      if (obj.isBNode) {
        val bn = obj.asInstanceOf[Rdf#BNode]
        bnodeClass.get(bn) orElse {
          val vt = VerticeType()
          bnodeClass.put(bn, vt)
          Some(vt)
        } map { vt =>
          vt.setBackwardRel(rel, subj)
        }
      }
    }
    bnodeClass.groupBy(_._2).mapValues(_.keys.toSet)
  }



  case class ClassificationException(msg: String,
                                     clz1: Map[VerticeType, Set[Rdf#BNode]],
                                     clz2: Map[VerticeType, Set[Rdf#BNode]]) extends MappingError {
    override def toString() = s"ClassificationException($msg,$clz1,$clz2)"
  }


  case class VerticeType(forwardRels: mutable.Map[Rdf#URI, Int] = mutable.HashMap().withDefaultValue(0),
                         backwardRels: mutable.Map[Rdf#URI, Int] = mutable.HashMap().withDefaultValue(0)) {

    def this(forward: List[(Rdf#URI, Int)],
             backward: List[(Rdf#URI, Int)]) = this(mutable.HashMap(forward: _*), mutable.HashMap(backward: _*))

    def setForwardRel(rel: Rdf#URI, obj: Rdf#Node) {
      forwardRels.put(rel, forwardRels(rel) + 1)
    }
    def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node) {
      backwardRels.put(rel, backwardRels(rel) + 1)
    }

  }


}
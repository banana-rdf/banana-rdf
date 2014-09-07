package org.w3.banana.iso

import org.w3.banana.{ RDF, RDFOps }

import scala.collection.immutable.ListMap
import scala.collection.{ mutable, immutable }
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success, Try }
import scalaz.Tree

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
class GraphIsomorphism[Rdf <: RDF](val mappingGen: MappingGenerator[Rdf], maxComplexity: Int = 65536)(implicit ops: RDFOps[Rdf]) {

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
   * create a tree out of lm where where each level consists of one key-value pair tree grafted onto each
   * of the previous levels, with a fake root level (0,0).
   * So Map(1->Set(1,2),2->Set(21,22)) will give a Tree with Root node (0,0) with children
   * <pre>
   * (1,1)--(2,21)
   *   |----(2,22)
   * (1,2)--(2,21)
   *   |----(2,22)
   *  </pre>
   * each of the paths from the root to the leaves constitues one possible solution
   */
  def tree[T](lm: immutable.ListMap[T, Set[T]])(root: (T, T)): Tree[(T, T)] = {
    def build(keys: List[T]): Stream[Tree[(T, T)]] = {
      keys match {
        case Nil => Stream()
        case head :: tail => for (root <- lm(keys.head).toStream.map((keys.head -> _)))
          yield Tree.node(root, build(keys.tail))
      }
    }
    Tree.node(root, build(lm.keys.toList))
  }

  /**
   * @param tree
   * @tparam T
   * @return all the paths from root to leaves for the given Tree
   */
  def branches[T](tree: Tree[T]): Stream[Stream[T]] =
    tree.subForest match {
      case Stream() => Stream(Stream(tree.rootLabel))
      case children => for {
        c <- children
        branch <- branches(c)
      } yield {
        tree.rootLabel #:: branch
      }
    }

  /**
   * Find possible bnode mappings
   * @param g1
   * @param g2
   * @return A list of possible bnode mappings or a reason for the error
   */
  def findPossibleMappings(g1: Rdf#Graph, g2: Rdf#Graph): Try[Stream[Stream[(Rdf#BNode, Rdf#BNode)]]] = {
    if (g1.size != g2.size)
      return Failure(MappingException(s"graphs don't have the same number of triples: g1.size=${g1.size} g2.size=${g2.size}"))

    val (grnd1, nongrnd1) = groundTripleFilter(g1)
    val (grnd2, nongrnd2) = groundTripleFilter(g2)
    if (grnd1.size != grnd2.size)
      return Failure(MappingException("the two graphs don't have the same number of ground triples. " +
        s"ground(g1).size=${grnd1.size} ground(g2).size=${grnd2.size}"))

    val bnodeMaps = mappingGen.bnodeMappings(nongrnd1, nongrnd2)
    val complexity = MappingGenerator.complexity(bnodeMaps)
    if (complexity > maxComplexity)
      return Failure(MappingException(s"Search space too big. maxComplexity is set to $maxComplexity but the search space is of size $complexity"))

    bnodeMaps map { listMap =>
      branches(tree[Rdf#BNode](listMap)(bnpair)).map(_.tail) //_tail: remove all the first elements
    }
  }

  val bnpair = (BNode(), BNode())

  def findAnswer(g1: Rdf#Graph, g2: Rdf#Graph): Try[List[(Rdf#BNode, Rdf#BNode)]] = {
    findPossibleMappings(g1, g2).flatMap { possibleAnswers =>
      val verifyAnswers = possibleAnswers.map { answer =>
        answer -> mapVerify(g1, g2, answer)
      }
      val answerOpt = verifyAnswers.find {
        case (s, err) =>
          //          println(s"===>$err = for answer ${s.toList}")
          err == Nil
      }
      answerOpt.map(a => Success(a._1.toList)).getOrElse(Failure(NoMappingException(verifyAnswers)))
    }
  }

  /**
   * Verify that the bnode bijection allows one to map graph1 to graph2
   * @param graph1
   * @param graph2
   * @param mapping  the mappings to verify.
   * @return a list of exceptions in case of failure or an empty list in case of success
   *
   */
  def mapVerify(graph1: Rdf#Graph, graph2: Rdf#Graph, mapping: Stream[(Rdf#BNode, Rdf#BNode)]): List[MappingException] = {
    // verify that both graphs are the same size
    if (graph1.size != graph2.size)
      return List(MappingException(s"graphs not of same size. graph1.size=${graph1.size} graph2.size=${graph2.size}"))

    //1. verify that bnodeBijection is a bijection, fail early
    val bnodeBijection = {
      var back = new mutable.HashMap[Rdf#BNode, Rdf#BNode]()
      var map = new mutable.HashMap[Rdf#BNode, Rdf#BNode]()
      for (m <- mapping) {
        if (back.get(m._2).fold(true)(bn => bn == m._1 && map.get(bn) == Some(m._2))) {
          back += m.swap
          if (map.put(m._1, m._2) != None) return List(MappingException(s"bnodeBijection is not a bijection: ${m._1} in $m already mapped."))
        } else return List(MappingException(s"bnodeBijection is not a bijection: $m maps to more than one value"))
      }
      map
    }

    def bnmap(node: Rdf#Node): Rdf#Node = node.fold(uri => uri, bnode => bnodeBijection(bnode), lit => lit)

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

  case class NoMappingException(val reasons: Stream[(Stream[(Rdf#BNode, Rdf#BNode)], List[MappingError])]) extends MappingError {
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
trait MappingGenerator[Rdf <: RDF] {
  /**
   * generate a list of possible bnode mappings.
   * A very stupid implementation would for each bnode in g1 suggest every bnode in g2 as a mapping.
   * Better implementations will filter the mappings by applying classification algorithm
   * on the nodes by relating them to other edges
   * @param g1 first graph
   * @param g2 second graph
   * @return a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which
   *         they should correspond to. The list map keeps an order of the nodes so as to put the nodes
   *         with the least options first.
   */
  def bnodeMappings(g1: Rdf#Graph, g2: Rdf#Graph): Try[immutable.ListMap[Rdf#BNode, immutable.Set[Rdf#BNode]]]

}

object MappingGenerator {
  /**
   * calculate the  size of the  tree of possibilities from the given mapping
   * @param maps
   * @tparam T
   */
  def complexity[T](maps: Try[Map[T, Set[T]]]) = {
    maps match {
      case x: Failure[T] => 0
      case Success(m) => m.values.foldLeft(1)((n, v) => n * v.size)
    }
  }
}

/*
 * The SimpleMappingGenerator implements only the first stage of Jeremy
 * Carroll's optimisation strategy. It classifies nodes only by the arrows
 * going in and out, but does not follow those further.
 *
 * @param VT the classifier
 * @param maxComplexity the maximum number of solutions to look at, otherwise fails
 * @param ops
 * @tparam Rdf
 */
class SimpleMappingGenerator[Rdf <: RDF](VT: VerticeTypeGenerator[Rdf])(implicit ops: RDFOps[Rdf])
    extends MappingGenerator[Rdf] {
  import ops._
  /**
   * generate a list of possible bnode mappings, filtered by applying classification algorithm
   * on the nodes by relating them to other edges
   * @param g1
   * @param g2
   * @return a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which
   *         they should corresond to
   */
  def bnodeMappings(g1: Rdf#Graph, g2: Rdf#Graph): Try[immutable.ListMap[Rdf#BNode, immutable.Set[Rdf#BNode]]] = Try {
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
    //todo: this transformation to immutable is expensive
    ListMap(mappingOpts.toSeq.map(l => (l._1, l._2.toSet)).sortBy(_._2.size): _*)
  }

  /**
   * This classification can be improved, but it is easier to debug while it is not so effective.
   * @param graph
   * @return a classification of bnodes by type, where nodes can only be matched by other nodes of the same type
   */
  def bnodeClassify(graph: Rdf#Graph): Map[VerticeType[Rdf], Set[Rdf#BNode]] = {
    val bnodeClass = mutable.HashMap[Rdf#BNode, VerticeType[Rdf]]()
    for (Triple(subj, rel, obj) <- graph.triples) {
      if (subj.isBNode) {
        val bn = subj.asInstanceOf[Rdf#BNode]
        bnodeClass.get(bn) orElse {
          val vt = VT()
          bnodeClass.put(bn, vt)
          Some(vt)
        } map { vt =>
          vt.setForwardRel(rel, obj)
        }
      }
      if (obj.isBNode) {
        val bn = obj.asInstanceOf[Rdf#BNode]
        bnodeClass.get(bn) orElse {
          val vt = VT()
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
      clz1: Map[VerticeType[Rdf], Set[Rdf#BNode]],
      clz2: Map[VerticeType[Rdf], Set[Rdf#BNode]]) extends MappingError {
    override def toString() = s"ClassificationException($msg,$clz1,$clz2)"
  }

}

trait VerticeType[Rdf <: RDF] {
  def forwardRels: mutable.Map[Rdf#URI, Long]
  def backwardRels: mutable.Map[Rdf#URI, Long]
  def setForwardRel(rel: Rdf#URI, obj: Rdf#Node): Unit
  def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node): Unit

}

/**
 * This is a stupidly simple VerticeType classifier.
 * It is useful to test the MappingGenerator though.
 * It classifies each vertice by the number of certain types of relations it has.
 * WARNING: Do not use in production! Checking isomorphisms with this class on a list with a
 * little duplication can make your machine stall. eg: comparing two graphs each containing the same
 * two lists of length 5 and 3 can lead to a search tree of 65 million. The SimpleHashVerticeType
 * brings that down to 64.
 * @param forwardRels forward relations
 * @param backwardRels backward relations
 * @tparam Rdf
 */
case class CountingVerticeType[Rdf <: RDF](
  val forwardRels: mutable.Map[Rdf#URI, Long],
  val backwardRels: mutable.Map[Rdf#URI, Long])
    extends VerticeType[Rdf] {

  def setForwardRel(rel: Rdf#URI, obj: Rdf#Node): Unit = {
    forwardRels.put(rel, forwardRels(rel) + 1)
  }
  def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node): Unit = {
    backwardRels.put(rel, backwardRels(rel) + 1)
  }

}

/**
 *
 * @param forwardRels
 * @param backwardRels
 * @param ops
 * @tparam Rdf
 */
case class SimpleHashVerticeType[Rdf <: RDF](
  val forwardRels: mutable.Map[Rdf#URI, Long],
  val backwardRels: mutable.Map[Rdf#URI, Long])(implicit ops: RDFOps[Rdf])
    extends VerticeType[Rdf] {
  import ops._

  val bnodeValue = 2017 // prime number

  def hashOf(node: Rdf#Node) = if (node.isBNode) bnodeValue else node.hashCode()

  def setForwardRel(rel: Rdf#URI, obj: Rdf#Node): Unit = {
    //todo: should this be an addition or also a modulus?  ( and below too )
    forwardRels.put(rel, forwardRels(rel) + hashOf(obj))
  }
  def setBackwardRel(rel: Rdf#URI, subj: Rdf#Node): Unit = {
    backwardRels.put(rel, backwardRels(rel) + hashOf(subj))
  }

}

trait VerticeTypeGenerator[Rdf <: RDF] {
  def apply(forwardRels: mutable.Map[Rdf#URI, Long] = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0),
    backwardRels: mutable.Map[Rdf#URI, Long] = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0)): VerticeType[Rdf]
}

object VT {
  def simpleHash[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = {
    new VerticeTypeGenerator[Rdf] {

      override def apply(
        forwardRels: mutable.Map[Rdf#URI, Long] = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0),
        backwardRels: mutable.Map[Rdf#URI, Long] = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0)) = {
        SimpleHashVerticeType(forwardRels, backwardRels)(ops)
      }
    }
  }

  def counting[Rdf <: RDF] = new VerticeTypeGenerator[Rdf] {
    override def apply(forwardRels: mutable.Map[Rdf#URI, Long] = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0),
      backwardRels: mutable.Map[Rdf#URI, Long] = mutable.HashMap[Rdf#URI, Long]().withDefaultValue(0)) =
      {
        CountingVerticeType(forwardRels, backwardRels)
      }
  }
}


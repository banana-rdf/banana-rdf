package org.w3.banana.rdfstorew

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.util.control.NoStackTrace
import scala.util.{Success, Failure, Try}

/**
 * Methods to establish Graph Equivalences
 *
 * Following Jeremy J. Carroll's "Matching RDF Graphs" article
 *  http://www.hpl.hp.com/techreports/2001/HPL-2001-293.pdf
 *
 * ( Currently the first part of the algorithm p10 has been implemented )
 *
 * Also I found it useful to think in terms of the RDF category
 * as defined by Benjamin Braatz in "Formal Modelling and Application of Graph Transformations
 * in the Resource Description Framework" Proposition 2.1 ( p 16 )
 *
 * http://www.researchgate.net/profile/Benjamin_Braatz/publication/40635984_Formal_Modelling_and_Application_of_Graph_Transformations_in_the_Resource_Description_Framework/file/d912f50d3189b51ef1.pdf
 *
 * As shown by Jeremy Carroll's paper this is only the first stage in the
 * optimisation. This code can be ameliorated in a number of ways:
 *
 *  - by increasing the sensitivity of categories as explained by Jeremy Caroll
 *  - by using lazy data structures
 *  - by optimising memory
 *
 *  Each optimisation makes thinking about the code harder though.
 */
object GraphEquivalence {

  import RDFStore.Ops._

  /**
   * filter a graphs into two subgraphs the first one containing no blank nodes
   * the other one containing only statements with blank nodes
   *
   * @param graph
   * @return a pair of Ground graph and non ground graph
   */
  def groundTripleFilter(graph: RDFStoreGraph): (RDFStoreGraph, RDFStoreGraph) = {
    var ground = emptyGraph
    var nonGround = emptyGraph
    for (triple <- graph.triples) {
      if(triple.subject.isInstanceOf[RDFStoreBlankNode] || triple.objectt.isInstanceOf[RDFStoreBlankNode]) {
        nonGround = nonGround.add(triple)
      } else {
        ground = ground.add(triple)
      }
    }
    (ground, nonGround)
  }

  /**
   * generate a list of possible bnode mappings, filtered by applying classification algorithm
   * on the nodes by relating them to other edges
   * @param g1
   * @param g2
   * @return a ListMap mapping RDFStoreBlankNode from graph g1 to a smaller set of Bnodes from graph g2 which
   *         they should corresond to
   */
  def bnodeMappingGenerator(g1: RDFStoreGraph, g2: RDFStoreGraph): Try[ListMap[RDFStoreBlankNode,mutable.Set[RDFStoreBlankNode]]] = Try {
    if (g1.size != g2.size)
      throw MappingException(s"graphs don't have the same number of triples: g1.size=${g1.size} g2.size=${g2.size}")
    val (grnd1, nongrnd1) = groundTripleFilter(g1)
    val (grnd2, nongrnd2) = groundTripleFilter(g2)
    if (grnd1.size != grnd2.size)
      throw MappingException("the two graphs don't have the same number of ground triples. " +
        s"ground(g1).size=${grnd1.size} ground(g2).size=${grnd2.size}")
    val clz1 = bnodeClassify(g1)
    val clz2 = bnodeClassify(g2)
    if (clz1.size != clz2.size)
      throw ClassificationException("the two graphs don't have the same number of classes.", clz1, clz2)
    val mappingOpts: mutable.Map[RDFStoreBlankNode,mutable.Set[RDFStoreBlankNode]] = mutable.HashMap[RDFStoreBlankNode,mutable.Set[RDFStoreBlankNode]]()
    for {
      (vt, bnds1) <- clz1 // .sortBy { case (vt, bn) => bn.size }
      bnds2 <- clz2.get(vt)
    } {
      if (bnds2.size != bnds1.size)
        throw ClassificationException(s"the two graphs don't have the same number of bindings for type $vt", clz1, clz2)
      for (bnd <- bnds1) {
        mappingOpts.get(bnd).orElse(Some(mutable.Set.empty[RDFStoreBlankNode])).map { bnset =>
          mappingOpts.put(bnd,bnset ++= bnds2)
        }
      }
    }
    ListMap(mappingOpts.toList.sortBy(_._2.size):_*)
  }

  /**
   * Find possible bnode mappings
   * @param g1
   * @param g2
   * @return A list of possible bnode mappings or a reason for the error
   */
  def findPossibleMappings(g1: RDFStoreGraph, g2: RDFStoreGraph): Try[List[List[(RDFStoreBlankNode,RDFStoreBlankNode)]]] =  {
    bnodeMappingGenerator(g1, g2) map { listMap =>
      val keys = listMap.keys.toList
      def tree(keys: List[RDFStoreBlankNode]): List[List[(RDFStoreBlankNode,RDFStoreBlankNode)]] = {
        keys match {
          case head::tail =>  listMap(keys.head).toList.flatMap{ mappedBN =>
            tree(tail).map((head,mappedBN)::_)
          }
          case Nil => List(List())
        }
      }
      tree(keys)
    }
  }

  def findAnswer(g1: RDFStoreGraph, g2: RDFStoreGraph): Try[List[(RDFStoreBlankNode,RDFStoreBlankNode)]] = {
    findPossibleMappings(g1,g2).flatMap{possibleAnswers =>
      val verifyAnswers= possibleAnswers.map(answer=>
        answer->mapVerify(g1,g2,Map(answer:_*))
      )
      val answerOpt = verifyAnswers.find{case (_,err)=>err==Nil}
      answerOpt.map(a =>Success(a._1)).getOrElse(Failure(NoMappingException(verifyAnswers)))
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
  def mapVerify(graph1: RDFStoreGraph, graph2: RDFStoreGraph, bnodeBijection: Map[RDFStoreBlankNode, RDFStoreBlankNode]): List[MappingException] = {
    def map(node: RDFStoreRDFNode) = node match {
      case bnode: RDFStoreBlankNode => bnodeBijection(bnode)
      case other => other
    }
    // verify that both graphs are the same size
    if (graph1.size != graph2.size)
      return List(MappingException(s"graphs not of same size. graph1.size=${graph1.size} graph2.size=${graph2.size}"))
    var graphTmp = graph2.dup

    //1. verify that bnodeBijection is a bijection ( it may be nicer to have a data structure that guarantees that)
    if (bnodeBijection.values.toSet.size != bnodeBijection.size)
      return List(MappingException(s"bnodeBijection is not a bijection: some keys map to more than one value"))

    try {
      for (triple <- graph1.toIterable) {
        val sub:RDFStoreRDFNode = triple.subject
        val rel:RDFStoreNamedNode = triple.predicate
        val obj:RDFStoreRDFNode = triple.objectt
        var mappedTriple: RDFStoreTriple = null
        try {
          mappedTriple = makeTriple(map(sub), rel, map(obj))
        } catch {
          case e: java.util.NoSuchElementException => {
            throw MappingException(s"could not find map for $sub or $obj")
          }
        }
        try {
          graphTmp = graphTmp.remove(mappedTriple)
        } catch {
          case e: java.util.NoSuchElementException => {
            throw MappingException(s"could not find map($triple)=$mappedTriple")
          }
        }
      }
    } catch {
      case e: MappingException => return List(e)
    }

    if (graphTmp.size == 0) List()
    else
      List(MappingException(s"should never return this exception! ${graphTmp.size} more triples graph2 than in graph1"))

    //do I have to test that the mapping goes the other way too? Or is it sufficient if the bnodesmap is a bijection?
  }

  /**
   * This classification can be improved, but it is easier to debug while it is not so effective.
   * @param graph
   * @return a classification of bnodes by type, where nodes can only be matched by other nodes of the same type
   */
  def bnodeClassify(graph: RDFStoreGraph): Map[VerticeType, Set[RDFStoreBlankNode]] = {
    val bnodeClass = mutable.HashMap[RDFStoreBlankNode, VerticeType]()
    for (triple <- graph.triples) {
      val subj:RDFStoreRDFNode = triple.subject
      val rel:RDFStoreNamedNode = triple.predicate
      val obj:RDFStoreRDFNode = triple.objectt

      if (subj.isInstanceOf[RDFStoreBlankNode]) {
        val bn = subj.asInstanceOf[RDFStoreBlankNode]
        bnodeClass.get(bn) orElse {
          val vt = VerticeType()
          bnodeClass.put(bn, vt)
          Some(vt)
        } map { vt =>
          vt.setForwardRel(rel, obj)
        }
      }
      if (obj.isInstanceOf[RDFStoreBlankNode]) {
        val bn = obj.asInstanceOf[RDFStoreBlankNode]
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

}

case class VerticeType(forwardRels: mutable.Map[RDFStoreNamedNode, Int] = mutable.HashMap().withDefaultValue(0),
                       backwardRels: mutable.Map[RDFStoreNamedNode, Int] = mutable.HashMap().withDefaultValue(0)) {

  def this(forward: List[(RDFStoreNamedNode, Int)],
           backward: List[(RDFStoreNamedNode, Int)]) = this(mutable.HashMap(forward: _*), mutable.HashMap(backward: _*))

  def setForwardRel(rel: RDFStoreNamedNode, obj: RDFStoreRDFNode) {
    forwardRels.put(rel, forwardRels(rel) + 1)
  }
  def setBackwardRel(rel: RDFStoreNamedNode, subj: RDFStoreRDFNode) {
    backwardRels.put(rel, backwardRels(rel) + 1)
  }

}

trait MappingError extends NoStackTrace {
  def msg: String
}

case class MappingException(msg: String) extends MappingError {
  override def toString() = s"MappingException($msg)"
}

case class NoMappingException(val reasons: List[(List[(RDFStoreBlankNode,RDFStoreBlankNode)],List[MappingError])]) extends MappingError {
  def msg = "No mapping found"
  override def toString() = s"NoMappingException($reasons)"
}

case class ClassificationException(msg: String,
                                   clz1: Map[VerticeType, Set[RDFStoreBlankNode]],
                                   clz2: Map[VerticeType, Set[RDFStoreBlankNode]]) extends MappingError {
  override def toString() = s"ClassificationException($msg,$clz1,$clz2)"
}
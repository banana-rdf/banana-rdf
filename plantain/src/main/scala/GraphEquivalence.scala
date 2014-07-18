package org.w3.banana.plantain

import org.w3.banana.plantain.model._

import scala.collection.mutable
import scala.util.Try

/**
 * Created by hjs on 12/07/2014.
 */
object GraphEquivalence {

  import PlantainOps._

  /**
   * filter a graphs into two subgraphs the first one containing no blank nodes
   * the other one containing only statements with blank nodes
   *
   * @param graph
   * @return a pair of Ground graph and non ground graph
   */
  def groundTripleFilter(graph: Graph): (Graph, Graph) = {
    var ground = Graph.empty
    var nonGround = Graph.empty
    for (triple <- graph.triples) {
      triple match {
        case Triple(s, r, o) if s.isInstanceOf[BNode] || o.isInstanceOf[BNode] => nonGround = nonGround + triple
        case _ => ground = ground + triple
      }
    }
    (ground, nonGround)
  }

  //add explanation later
  def bnodeMappingGenerator(g1: Graph, g2: Graph): Try[List[List[(BNode, BNode)]]] = Try {
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
    for {
      (vt, bnds1) <- clz1.toList.sortBy { case (vt, bn) => bn.size }
      bnds2 <- clz2.get(vt).toList.map(_.toList)
    } yield {
      if (bnds2.size != bnds1.size)
        throw ClassificationException(s"the two graphs don't have the same number of bindings for type $vt", clz1, clz2)
      for {
        bn1 <- bnds1.toList
        bn2 <- bnds2
      } yield {
        (bn1, bn2)
      }
    }

  }

  /**
   *
   * @param graph1
   * @param graph2
   * @param bnodeBijection
   * @return a list of exceptions in case of failure or an empty list in case of success
   */
  def mapVerify(graph1: Graph, graph2: Graph, bnodeBijection: Map[BNode, BNode]): List[MappingException] = {
    def map(node: Node) = node match {
      case bnode: BNode => bnodeBijection(bnode)
      case other => other
    }
    // verify that both graphs are the same size
    if (graph1.size != graph2.size)
      return List(MappingException(s"graphs not of same size. graph1.size=${graph1.size} graph2.size=${graph2.size}"))
    var graphTmp = graph2

    try {
      //todo: verify that bnodeBijection is a bijection
      for (triple <- graph1.toIterable) {
        val Triple(sub, rel, obj) = triple
        var mappedTriple: Triple = null
        try {
          mappedTriple = Triple(map(sub), rel, map(obj))
          println(s"map($triple)=>$mappedTriple")
        } catch {
          case e: java.util.NoSuchElementException => {
            println("cought " + e)
            throw MappingException(s"could not find map for $sub or $obj")
          }
        }
        try {
          graphTmp = graphTmp.removeExistingTriple(mappedTriple)
        } catch {
          case e: java.util.NoSuchElementException => {
            println("cought " + e)
            throw MappingException(s"could not find map($triple)=$mappedTriple")
          }
        }
      }
    } catch {
      case e: MappingException => return List(e)
      case nse => println(nse)
    }

    if (graphTmp.size == 0) List()
    else
      List(MappingException(s"should never return this exception! ${graphTmp.size} more triples graph2 than in graph1"))

    //do I have to test that the mapping goes the other way too? Or is it sufficient if the bnodesmap is a bijection?
  }

  def bnodeClassify(graph: Graph): Map[VerticeType, Set[BNode]] = {
    val bnodeClass = mutable.HashMap[BNode, VerticeType]()
    for (Triple(subj, rel, obj) <- graph.triples) {
      if (subj.isInstanceOf[BNode]) {
        val bn = subj.asInstanceOf[BNode]
        bnodeClass.get(bn) orElse {
          val vt = VerticeType()
          bnodeClass.put(bn, vt)
          Some(vt)
        } map { vt =>
          vt.setForwardRel(rel, obj)
        }
      }
      if (obj.isInstanceOf[BNode]) {
        val bn = obj.asInstanceOf[BNode]
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

case class VerticeType(forwardRels: mutable.Map[URI, Int] = mutable.HashMap().withDefaultValue(0),
    backwardRels: mutable.Map[URI, Int] = mutable.HashMap().withDefaultValue(0)) {

  def this(forward: List[(URI, Int)],
    backward: List[(URI, Int)]) = this(mutable.HashMap(forward: _*), mutable.HashMap(backward: _*))

  def setForwardRel(rel: URI, obj: Node) {
    forwardRels.put(rel, forwardRels(rel) + 1)
  }
  def setBackwardRel(rel: URI, subj: Node) {
    backwardRels.put(rel, backwardRels(rel) + 1)
  }

}

case class MappingException(msg: String) extends Throwable(msg)

case class ClassificationException(msg: String,
  clz1: Map[VerticeType, Set[BNode]],
  clz2: Map[VerticeType, Set[BNode]]) extends Throwable(msg)
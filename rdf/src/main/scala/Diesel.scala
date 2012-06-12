package org.w3.banana

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._
import NodeBinder._

object Diesel {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf], union: GraphUnion[Rdf], graphTraversal: RDFGraphTraversal[Rdf]): Diesel[Rdf] =
    new Diesel()(ops, union, graphTraversal)
}

class Diesel[Rdf <: RDF](
    implicit val ops: RDFOperations[Rdf],
    val graphUnion: GraphUnion[Rdf],
    val graphTraversal: RDFGraphTraversal[Rdf])
extends CommonBinders[Rdf] {

  import ops._
  import graphUnion._
  import graphTraversal._

  val xsd = XSDPrefix(ops)
  val rdf = RDFPrefix(ops)

  implicit def nodeBinder2PointedGraphBinder[T](implicit nodeBinder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] =
    new PointedGraphBinder[Rdf, T] {

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] =
        nodeBinder.fromNode(pointed.node)

      def toPointedGraph(t: T): PointedGraph[Rdf] = PointedGraph(nodeBinder.toNode(t), ops.Graph.empty)

    }


  class PointedGraphW(pointed: PointedGraph[Rdf]) {

    import pointed.graph

    def as[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      binder.fromPointedGraph(pointed)

    def asString: Validation[BananaException, String] = as[String]
    
    def asInt: Validation[BananaException, Int] = as[Int]
    
    def asDouble: Validation[BananaException, Double] = as[Double]

    def a(clazz: Rdf#URI): PointedGraph[Rdf] = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      PointedGraph(node, newGraph)
    }

    def --(p: Rdf#URI): PointedGraphPredicate = new PointedGraphPredicate(pointed, p)

    def -<-(p: Rdf#URI): PredicatePointedGraph = new PredicatePointedGraph(p, pointed)

    def /(p: Rdf#URI): PointedGraphs[Rdf] = {
      val nodes = getObjects(graph, node, p)
      PointedGraphs(nodes, graph)
    }

    def node: Rdf#Node = pointed.node

    def predicates = getPredicates(graph, node)

  }

  class PointedGraphsW(pointedGraphs: PointedGraphs[Rdf]) extends Iterable[PointedGraph[Rdf]] {

    import pointedGraphs.{ nodes, graph }

    def iterator = nodes.iterator map { PointedGraph(_, graph) }

    def /(p: Rdf#URI): PointedGraphs[Rdf] = {
      val ns = this flatMap { case PointedGraph(node, graph) => getObjects(graph, node, p) }
      PointedGraphs(ns, graph)
    }

    def takeOnePointedGraph: Validation[BananaException, PointedGraph[Rdf]] = {
      val it = nodes.iterator
      if (! it.hasNext) {
        Failure(WrongExpectation("expected exactly one node but got 0"))
      } else {
        val first = it.next
        Success(PointedGraph(first, graph))
      }
    }

    def takeOneAs[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      takeOnePointedGraph flatMap (_.as[T])

    def exactlyOnePointedGraph: Validation[BananaException, PointedGraph[Rdf]] = {
      val it = nodes.iterator
      if (! it.hasNext) {
        Failure(WrongExpectation("expected exactly one node but got 0"))
      } else {
        val first = it.next
        if (it.hasNext)
          Failure(WrongExpectation("expected exactly one node but got more than 1"))
        else
          Success(PointedGraph(first, graph))
      }
    }

    def as[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, T] =
      exactlyOnePointedGraph flatMap (_.as[T])

    def asOption[T](implicit binder: PointedGraphBinder[Rdf, T]): Validation[BananaException, Option[T]] = nodes.headOption match {
      case None => Success(None)
      case Some(node) => node.as[T] map (Some(_))
    }

  }

  class PointedGraphPredicate(pointed: PointedGraph[Rdf], p: Rdf#URI) {

    def ->-(o: Rdf#Node, os: Rdf#Node*): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph =
        if (os.isEmpty) {
          acc union Graph(Triple(s, p, o))
        } else {
          val triples: Iterable[Rdf#Triple] = (o :: os.toList) map { o => Triple(s, p, o) }
          Graph(triples) union acc
        }
      PointedGraph(s, graph)
    }

    def ->-[T](o: T)(implicit binder: NodeBinder[Rdf, T]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph = acc union Graph(Triple(s, p, binder.toNode(o)))
      PointedGraph(s, graph)
    }

    def -->-[T](o: T)(implicit binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val PointedGraph(oMainSubject, oGraph) = binder.toPointedGraph(o)
      val triple = Triple(s, p, oMainSubject)
      val graph = acc union oGraph union Graph(List(triple))
      PointedGraph(s, graph)
    }

    def ->-[T1, T2](o1: T1, o2: T2)(implicit b1: NodeBinder[Rdf, T1], b2: NodeBinder[Rdf, T2]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val graph = acc union Graph(Triple(s, p, b1.toNode(o1)), Triple(s, p, b2.toNode(o2)))
      PointedGraph(s, graph)
    }

    def ->-(pointedObject: PointedGraph[Rdf]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val PointedGraph(o, graphObject) = pointedObject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      PointedGraph(s, graph)
    }

    def ->-[T](opt: Option[T])(implicit binder: NodeBinder[Rdf, T]): PointedGraph[Rdf] = opt match {
      case None => pointed
      case Some(t) => this.->-(t)(binder)
    }

  }


  case class PredicatePointedGraph(p: Rdf#URI, pointed: PointedGraph[Rdf]) {

    def --(s: Rdf#Node): PointedGraph[Rdf] = {
      val PointedGraph(o, acc) = pointed
      val graph = acc union Graph(Triple(s, p, o))
      PointedGraph(s, graph)
    }

    def --(pointedSubject: PointedGraph[Rdf]): PointedGraph[Rdf] = {
      val PointedGraph(o, acc) = pointed
      val PointedGraph(s, graphObject) = pointedSubject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      PointedGraph(s, graph)
    }

  }

  class GraphW(graph: Rdf#Graph) {

    def getAllInstancesOf(clazz: Rdf#URI): PointedGraphs[Rdf] = {
      val instances = getSubjects(graph, rdf("type"), clazz): Iterable[Rdf#Node]
      PointedGraphs(instances, graph)
    }

  }

  implicit def node2PointedGraph(implicit node: Rdf#Node): PointedGraph[Rdf] = PointedGraph(node, Graph.empty)

  implicit def node2PointedGraphW(node: Rdf#Node): PointedGraphW = new PointedGraphW(new PointedGraph[Rdf](node, Graph.empty))

  implicit def pointedGraph2PointedGraphW(pointed: PointedGraph[Rdf]): PointedGraphW = new PointedGraphW(pointed)

  implicit def multiplePointedGraph2PointedGraphsW(pointedGraphs: PointedGraphs[Rdf]): PointedGraphsW = new PointedGraphsW(pointedGraphs)

  implicit def graph2GraphW(graph: Rdf#Graph): GraphW = new GraphW(graph)

  def bnode(): Rdf#BNode = BNode()

  def bnode(label: String) = BNode(label)

  def uri(s: String): Rdf#URI = URI(s)

}

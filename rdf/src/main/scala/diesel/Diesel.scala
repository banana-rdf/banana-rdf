package org.w3.banana.diesel

import org.w3.banana._
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._

object Diesel {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf], union: GraphUnion[Rdf], graphTraversal: RDFGraphTraversal[Rdf]): Diesel[Rdf] =
    new Diesel(ops, union, graphTraversal)
}

class Diesel[Rdf <: RDF](
  ops: RDFOperations[Rdf],
  union: GraphUnion[Rdf],
  graphTraversal: RDFGraphTraversal[Rdf]) {

  import ops._
  import union._
  import graphTraversal._

  val commonLiteralBinders = CommonLiteralBinders(ops)
  import commonLiteralBinders._

  val rdf = RDFPrefix(ops)

  class NodeW(node: Rdf#Node) {

    def as[T](implicit binder: LiteralBinder[Rdf, T]): Validation[Throwable, T] = {
      val literalV = fromTryCatch {
        Node.fold(node)(
          iri => sys.error("asLiteral: " + node.toString + " is not a literal"),
          bnode => sys.error("asLiteral: " + node.toString + " is not a literal"),
          literal => literal
        )
      }
      literalV flatMap { literal => binder.fromLiteral(literal) }
    }
      
    def asString: Validation[Throwable, String] = as[String]
    
    def asInt: Validation[Throwable, Int] = as[Int]
    
    def asDouble: Validation[Throwable, Double] = as[Double]

    def asURI: Validation[Throwable, Rdf#IRI] = fromTryCatch {
      Node.fold(node)(
        iri => iri,
        bnode => sys.error("asUri: " + node.toString + " is not a URI"),
        literal => sys.error("asUri: " + node.toString + " is not a URI")
      )
    }

  }

  class PointedGraphW(pointed: PointedGraph[Rdf]) {

    import pointed.{ node => _node , graph }

    def a(clazz: Rdf#IRI): PointedGraph[Rdf] = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      PointedGraph(node, newGraph)
    }

    def --(p: Rdf#IRI): PointedGraphPredicate = new PointedGraphPredicate(pointed, p)

    def -<-(p: Rdf#IRI): PredicatePointedGraph = new PredicatePointedGraph(p, pointed)

    def /(p: Rdf#IRI): PointedGraphs[Rdf] = {
      val nodes = getObjects(graph, node, p)
      PointedGraphs(nodes, graph)
    }

    def node: Rdf#Node = _node

    def predicates = getPredicates(graph, node)

  }

  class PointedGraphsW(pointedGraphs: PointedGraphs[Rdf]) extends Iterable[PointedGraph[Rdf]] {

    import pointedGraphs.{ nodes, graph }

    def iterator = nodes.iterator map { PointedGraph(_, graph) }

    def /(p: Rdf#IRI): PointedGraphs[Rdf] = {
      val ns = this flatMap { case PointedGraph(node, graph) => getObjects(graph, node, p) }
      PointedGraphs(ns, graph)
    }

    def takeOne: Validation[Throwable, Rdf#Node] = fromTryCatch { this.head.node }

    def exactlyOne: Validation[Throwable, Rdf#Node] = {
      val it = nodes.iterator
      val first = it.next
      if (first == null)
        Failure(new Exception("exactlyOne: not even one node"))
      else if (it.hasNext)
        Failure(new Exception("exactlyOne: more that one node"))
      else
        Success(first)
    }
    
  }

  class PointedGraphPredicate(pointed: PointedGraph[Rdf], p: Rdf#IRI) {

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

    def ->-(pointedObject: PointedGraph[Rdf]): PointedGraph[Rdf] = {
      val PointedGraph(s, acc) = pointed
      val PointedGraph(o, graphObject) = pointedObject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      PointedGraph(s, graph)
    }

    def ->-(collection: List[Rdf#Node]): PointedGraph[Rdf] = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      collection.reverse foreach { a =>
        val newBNode = BNode()
        triples += Triple(newBNode, rdf.first, a)
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      val PointedGraph(s, acc) = pointed
      triples += Triple(s, p, current)
      val graph = acc union Graph(triples)
      PointedGraph(current, Graph(triples))
    }

  }


  case class PredicatePointedGraph(p: Rdf#IRI, pointed: PointedGraph[Rdf]) {

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

  implicit def node2NodeW(node: Rdf#Node): NodeW = new NodeW(node)

  implicit def node2PointedGraphW(node: Rdf#Node): PointedGraphW = new PointedGraphW(new PointedGraph[Rdf](node, Graph.empty))

  implicit def pointedGraph2PointedGraphW(pointed: PointedGraph[Rdf]): PointedGraphW = new PointedGraphW(pointed)

  implicit def multiplePointedGraph2PointedGraphsW(pointedGraphs: PointedGraphs[Rdf]): PointedGraphsW = new PointedGraphsW(pointedGraphs)

  def bnode(): Rdf#BNode = BNode()

  def bnode(label: String) = BNode(label)

  def uri(s: String): Rdf#IRI = IRI(s)

}

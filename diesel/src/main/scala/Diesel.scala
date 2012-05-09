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

  val projections = RDFNodeProjections(ops)

  val rdf = RDFPrefix(ops)

  case class GraphNode(node: Rdf#Node, graph: Rdf#Graph) {

    def a(clazz: Rdf#IRI): GraphNode = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      GraphNode(node, newGraph)
    }

    def --(p: Rdf#IRI): GraphNodePredicate = GraphNodePredicate(this, p)

    def -<-(p: Rdf#IRI): PredicateGraphNode = PredicateGraphNode(p, this)

    def /(p: Rdf#IRI): GraphNodes = {
      val nodes = getObjects(graph, node, p)
      GraphNodes(nodes, graph)
    }

    def asString: Validation[Throwable, String] = projections.asString(node)
    
    def asInt: Validation[Throwable, Int] = projections.asInt(node)
    
    def asDouble: Validation[Throwable, Double] = projections.asDouble(node)

  }

  implicit def node2GraphNode(node: Rdf#Node): GraphNode = GraphNode(node, Graph.empty)

  case class GraphNodes(nodes: Iterable[Rdf#Node], graph: Rdf#Graph) extends Iterable[GraphNode] {

    def iterator = nodes.iterator map { GraphNode(_, graph) }

    def /(p: Rdf#IRI): GraphNodes = {
      val ns = this flatMap { case GraphNode(node, graph) => getObjects(graph, node, p) }
      GraphNodes(ns, graph)
    }

    def node: Validation[Throwable, Rdf#Node] = fromTryCatch { this.head.node }
    
    def asString: Validation[Throwable, String] = fromTryCatch { this.head.node } flatMap { _.asString }
    
    def asInt: Validation[Throwable, Int] = fromTryCatch { this.head.node } flatMap { _.asInt }
    
    def asDouble: Validation[Throwable, Double] = fromTryCatch { this.head.node } flatMap { _.asDouble }


  }

  case class GraphNodePredicate(graphNode: GraphNode, p: Rdf#IRI) {

    def ->-(o: Rdf#Node, os: Rdf#Node*): GraphNode = {
      val GraphNode(s, acc) = graphNode
      val graph =
        if (os.isEmpty) {
          acc union Graph(Triple(s, p, o))
        } else {
          val triples: Iterable[Rdf#Triple] = (o :: os.toList) map { o => Triple(s, p, o) }
          Graph(triples) union acc
        }
      GraphNode(s, graph)
    }

    def ->-(graphNodeObject: GraphNode): GraphNode = {
      val GraphNode(s, acc) = graphNode
      val GraphNode(o, graphObject) = graphNodeObject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      GraphNode(s, graph)
    }

    def ->-(collection: List[Rdf#Node]): GraphNode = {
      var current: Rdf#Node = rdf.nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      collection.reverse foreach { a =>
        val newBNode = BNode()
        triples += Triple(newBNode, rdf.first, a)
        triples += Triple(newBNode, rdf.rest, current)
        current = newBNode
      }
      val GraphNode(s, acc) = graphNode
      triples += Triple(s, p, current)
      val graph = acc union Graph(triples)
      GraphNode(current, Graph(triples))
    }

  }


  case class PredicateGraphNode(p: Rdf#IRI, graphNode: GraphNode) {

    def --(s: Rdf#Node): GraphNode = {
      val GraphNode(o, acc) = graphNode
      val graph = acc union Graph(Triple(s, p, o))
      GraphNode(s, graph)
    }

    def --(graphNodeSubject: GraphNode): GraphNode = {
      val GraphNode(o, acc) = graphNode
      val GraphNode(s, graphObject) = graphNodeSubject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      GraphNode(s, graph)
    }

  }


  def bnode(): Rdf#BNode = BNode()

  def bnode(label: String) = BNode(label)

  def uri(s: String): Rdf#IRI = IRI(s)


}

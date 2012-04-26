package org.w3.rdf.diesel

import org.w3.rdf._
import scalaz._
import scalaz.Scalaz._

abstract class Diesel[Rdf <: RDF](
  val ops: RDFOperations[Rdf],
  val union: GraphUnion[Rdf],
  val projections: Projections[Rdf]) {

  import ops._
  import union._
  import projections._

  case class GraphNode(node: Rdf#Node, graph: Rdf#Graph) {

    def a(clazz: Rdf#IRI): GraphNode = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      GraphNode(node, newGraph)
    }

    def --(p: Rdf#IRI): GraphNodePredicate = GraphNodePredicate(this, p)

    def -<-(p: Rdf#IRI): PredicateGraphNode = PredicateGraphNode(p, this)

    def /(p: Rdf#IRI): Iterable[GraphNode] =
      getObjects(graph, node, p) map { GraphNode(_, graph) }

  }

  implicit def wrapNodeInGraphNode(node: Rdf#Node): GraphNode = GraphNode(node, Graph.empty)

    val first = rdf("first")
    val rest = rdf("rest")
    val nil = rdf("nil")

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
      var current: Rdf#Node = nil
      val triples = scala.collection.mutable.Set[Rdf#Triple]()
      collection.reverse foreach { a =>
        val newBNode = BNode()
        triples += Triple(newBNode, first, a)
        triples += Triple(newBNode, rest, current)
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

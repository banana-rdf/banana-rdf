package org.w3.rdf.diesel

import org.w3.rdf._

abstract class Diesel[Rdf <: RDF](val ops: RDFOperations[Rdf], val union: GraphUnion[Rdf]) {

  import ops._
  import union._

  case class GraphNode(node: Node, graph: Graph) {
    def a(clazz: IRI): GraphNode = {
      val newGraph = graph union Graph(Triple(node, rdf("type"), clazz))
      GraphNode(node, newGraph)
    }
    def --(p: IRI): GraphNodePredicate = GraphNodePredicate(this, p)
    def -<-(p: IRI): PredicateGraphNode = PredicateGraphNode(p, this)
  }

  implicit def wrapNodeInGraphNode(node: Node): GraphNode = GraphNode(node, Graph.empty)

  case class GraphNodePredicate(graphNode: GraphNode, p: IRI) {

    def ->-(o: Node): GraphNode = {
      val GraphNode(s, acc) = graphNode
      val graph = acc union Graph(Triple(s, p, o))
      GraphNode(s, graph)
    }

    def ->-(graphNodeObject: GraphNode): GraphNode = {
      val GraphNode(s, acc) = graphNode
      val GraphNode(o, graphObject) = graphNodeObject
      val graph = Graph(Triple(s, p, o)) union acc union graphObject
      GraphNode(s, graph)
    }

  }


  case class PredicateGraphNode(p: IRI, graphNode: GraphNode) {

    def --(s: Node): GraphNode = {
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


  def bnode(): BNode = BNode()

  def bnode(label: String) = BNode(label)

  def uri(s: String): IRI = IRI(s)


}

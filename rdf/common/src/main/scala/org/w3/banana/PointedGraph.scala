package org.w3.banana

/*
 * A pointer into a graph,  enabling an object oriented way to traverse
 * the graph of relations.
 *
 * Note: because a pointer can be a blank node, and the blank node only
 * has meaning within the graph, there is no way to compare two pointed graphs
 * in the general case. Hence we use Object equality.
 */
trait PointedGraph[Rdf <: RDF] {

  def pointer: Rdf#Node

  def graph: Rdf#Graph

}

object PointedGraph {

  def apply[Rdf <: RDF](_pointer: Rdf#Node, _graph: Rdf#Graph): PointedGraph[Rdf] =
    new PointedGraph[Rdf] {
      val pointer = _pointer
      val graph = _graph
    }

  def apply[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] =
    PointedGraph[Rdf](node, ops.emptyGraph)

  def unapply[Rdf <: RDF](pg: PointedGraph[Rdf]): Option[(Rdf#Node, Rdf#Graph)] = Some((pg.pointer, pg.graph))

}

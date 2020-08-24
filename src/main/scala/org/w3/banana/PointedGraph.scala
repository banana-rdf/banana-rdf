package org.w3.banana

import org.w3.banana._

type RDFObj = RDF & Singleton

trait RDFOps[T <: RDFObj](using val rdf: T) {
   def emptyGraph: rdf.Graph
   def fromUri(uri: rdf.URI): String
   def makeUri(s: String): rdf.URI
}


trait PointedGraph[T <: RDFObj](using val rdf: T) {
  def pointer: rdf.Node
  def graph: rdf.Graph
}

object PointedGraph {
  def apply[T <: RDFObj](using rdf:T)(
    node: rdf.Node,
    inGraph: rdf.Graph
  ): PointedGraph[rdf.type] =
    new PointedGraph[rdf.type](){
      val pointer = node
      val graph = inGraph
    }

  def apply[T <: RDFObj](using rdf: T) (
    node: rdf.Node
  )(using ops: RDFOps[rdf.type]): PointedGraph[rdf.type] =
    new PointedGraph[rdf.type]() {
      val pointer = node
      val graph   = ops.emptyGraph
    }

  def unapply[T <: RDFObj](using rdf: T)(
    pg: PointedGraph[rdf.type]
  )(using ops: RDFOps[T]): (rdf.Node, rdf.Graph) = (pg.pointer, pg.graph)

}

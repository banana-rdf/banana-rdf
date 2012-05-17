package org.w3.banana

trait PointedGraphBinder[Rdf <: RDF, T] {
  def fromPointedGraph(pointed: PointedGraph[Rdf]): T
  def toPointedGraph(t: T): PointedGraph[Rdf]
}

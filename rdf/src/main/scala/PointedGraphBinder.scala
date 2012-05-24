package org.w3.banana

import scalaz.Validation

trait PointedGraphBinder[Rdf <: RDF, T] {
  def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T]
  def toPointedGraph(t: T): PointedGraph[Rdf]
}

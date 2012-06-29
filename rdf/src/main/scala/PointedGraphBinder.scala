package org.w3.banana

import scalaz.Validation

trait PointedGraphBinder[Rdf <: RDF, T] extends PGB[Rdf, T, T]{
  // def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T]
  // def toPointedGraph(t: T): PointedGraph[Rdf]
}

trait PGB[Rdf <: RDF, +S, -T] {
  def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, S]
  def toPointedGraph(t: T): PointedGraph[Rdf]
}

object PointedGraphBinder {

  def apply[Rdf <: RDF, T](implicit binder: PointedGraphBinder[Rdf, T]): PointedGraphBinder[Rdf, T] = binder

}

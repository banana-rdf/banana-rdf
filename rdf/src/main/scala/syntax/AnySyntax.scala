package org.w3.banana.syntax

import org.w3.banana._

class AnyW[Rdf <: RDF, T](t: T) {

  def toUri(implicit binder: URIBinder[Rdf, T]): Rdf#URI = binder.toUri(t)

  def toPointedGraph(implicit binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = binder.toPointedGraph(t)

  def toPG(implicit binder: PointedGraphBinder[Rdf, T]): PointedGraph[Rdf] = this.toPointedGraph(binder)

}

trait AnySyntax[Rdf <: RDF] {
  implicit def anyToAnyW[T](t: T): AnyW[Rdf, T] = new AnyW[Rdf, T](t)
}

package org.w3.banana.syntax

import org.w3.banana._

class AnyWithToURIW[Rdf <: RDF, T](t: T, uriMaker: ToURI[Rdf, T]) {

  def toUri(implicit uriMaker: ToURI[Rdf, T]): Rdf#URI = uriMaker.toUri(t)

}

class AnyWithToPG[Rdf <: RDF, T](t: T, pgMaker: ToPointedGraph[Rdf, T]) {

  def toPointedGraph: PointedGraph[Rdf] = pgMaker.toPointedGraph(t)

  def toPG: PointedGraph[Rdf] = toPointedGraph

}

class AnyWithNodeBinderW[Rdf <: RDF, T](t: T, nodeBinder: NodeBinder[Rdf, T]) {

  def toNode: Rdf#Node = nodeBinder.toNode(t)

}

trait AnySyntax[Rdf <: RDF] {

  implicit def anyToAnyWithToURIW[T](t: T)(implicit uriMaker: ToURI[Rdf, T]): AnyWithToURIW[Rdf, T] = new AnyWithToURIW[Rdf, T](t, uriMaker)

  implicit def anyToAnyWithToPG[T](t: T)(implicit pgMaker: ToPointedGraph[Rdf, T]): AnyWithToPG[Rdf, T] = new AnyWithToPG[Rdf, T](t, pgMaker)

  implicit def anyToAnyWithNodeBinderW[T](t: T)(implicit nodeBinder: NodeBinder[Rdf, T]): AnyWithNodeBinderW[Rdf, T] = new AnyWithNodeBinderW[Rdf, T](t, nodeBinder)

}

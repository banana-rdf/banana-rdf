package org.w3.banana.syntax

import org.w3.banana._

class AnyWithToURIW[Rdf <: RDF, T](t: T, uriMaker: ToURI[Rdf, T]) {

  def toUri(implicit uriMaker: ToURI[Rdf, T]): Rdf#URI = uriMaker.toUri(t)

}

class AnyWithPGBW[Rdf <: RDF, T](t: T, binder: PointedGraphBinder[Rdf, T]) {

  def toPointedGraph: PointedGraph[Rdf] = binder.toPointedGraph(t)

  def toPG: PointedGraph[Rdf] = toPointedGraph

}

class AnyWithNodeBinderW[Rdf <: RDF, T](t: T, nodeBinder: NodeBinder[Rdf, T]) {

  def toNode: Rdf#Node = nodeBinder.toNode(t)

}

trait AnySyntax[Rdf <: RDF] {

  implicit def anyToAnyWithToURIW[T](t: T)(implicit uriMaker: ToURI[Rdf, T]): AnyWithToURIW[Rdf, T] = new AnyWithToURIW[Rdf, T](t, uriMaker)

  implicit def anyToAnyWithPBGW[T](t: T)(implicit binder: PointedGraphBinder[Rdf, T]): AnyWithPGBW[Rdf, T] = new AnyWithPGBW[Rdf, T](t, binder)

  implicit def anyToAnyWithNodeBinderW[T](t: T)(implicit nodeBinder: NodeBinder[Rdf, T]): AnyWithNodeBinderW[Rdf, T] = new AnyWithNodeBinderW[Rdf, T](t, nodeBinder)

}

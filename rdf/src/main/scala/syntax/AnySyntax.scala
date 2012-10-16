package org.w3.banana.syntax

import org.w3.banana._

class AnySyntax[Rdf <: RDF, T](val t: T) extends AnyVal {

  def toUri(implicit uriMaker: ToURI[Rdf, T]): Rdf#URI = uriMaker.toUri(t)

  def toPointedGraph(implicit pgMaker: ToPointedGraph[Rdf, T]): PointedGraph[Rdf] = pgMaker.toPointedGraph(t)

  def toPG(implicit pgMaker: ToPointedGraph[Rdf, T]): PointedGraph[Rdf] = toPointedGraph

  def toNode(implicit nodeBinder: NodeBinder[Rdf, T]): Rdf#Node = nodeBinder.toNode(t)

}

//class AnySyntax[T](val t: T) extends AnyVal {
//
//  def toUri[Rdf <: RDF](implicit uriMaker: ToURI[Rdf, T]): Rdf#URI = uriMaker.toUri(t)
//
//  def toPointedGraph[Rdf <: RDF](implicit pgMaker: ToPointedGraph[Rdf, T]): PointedGraph[Rdf] = pgMaker.toPointedGraph(t)
//
//  def toPG[Rdf <: RDF](implicit pgMaker: ToPointedGraph[Rdf, T]): PointedGraph[Rdf] = toPointedGraph
//
//  def toNode[Rdf <: RDF](implicit nodeBinder: NodeBinder[Rdf, T]): Rdf#Node = nodeBinder.toNode(t)
//
//}

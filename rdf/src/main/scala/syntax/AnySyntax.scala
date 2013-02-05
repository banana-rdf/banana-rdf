package org.w3.banana.syntax

import org.w3.banana._
import org.w3.banana.binder._

trait AnySyntax {

  implicit def anyW[T](t: T): AnyW[T] = new AnyW[T](t)

}

object AnySyntax extends AnySyntax

class AnyW[T](val t: T) extends AnyVal {

  def toUri[Rdf <: RDF](implicit uriMaker: ToURI[Rdf, T]): Rdf#URI = uriMaker.toURI(t)

  def toNode[Rdf <: RDF](implicit to: ToNode[Rdf, T]): Rdf#Node = to.toNode(t)

  def toPointedGraph[Rdf <: RDF](implicit to: ToPG[Rdf, T]): PointedGraph[Rdf] = to.toPG(t)

  def toPG[Rdf <: RDF](implicit to: ToPG[Rdf, T]): PointedGraph[Rdf] = this.toPointedGraph(to)

}

package org.w3.banana.syntax

import org.w3.banana._
import org.w3.banana.binder._

trait AnySyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def anyW[T](t: T): AnyW[Rdf, T] = new AnyW[Rdf, T](t)

}

class AnyW[Rdf <: RDF, T](val t: T) extends AnyVal {

  def toUri(implicit uriMaker: ToURI[Rdf, T]): Rdf#URI = uriMaker.toURI(t)

  def toNode(implicit to: ToNode[Rdf, T]): Rdf#Node = to.toNode(t)

  def toPointedGraph(implicit to: ToPG[Rdf, T]): PointedGraph[Rdf] = to.toPG(t)

  def toPG(implicit to: ToPG[Rdf, T]): PointedGraph[Rdf] = this.toPointedGraph(to)

}

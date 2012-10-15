package org.w3.banana

import scala.util._

trait ToURI[Rdf <: RDF, T] extends Any {
  def toUri(t: T): Rdf#URI
}

trait FromURI[Rdf <: RDF, T] extends Any {
  def fromUri(uri: Rdf#URI): Try[T]
}

trait URIBinder[Rdf <: RDF, T] extends Any with FromURI[Rdf, T] with ToURI[Rdf, T]

class URIBinderW[Rdf <: RDF, T](binder: URIBinder[Rdf, T]) {
  def uriMaker[C](f: C => T): ToURI[Rdf, C] = new ToURI[Rdf, C] {
    def toUri(c: C): Rdf#URI = binder.toUri(f(c))
  }
}

object URIBinder {

  implicit def toURIBinderW[Rdf <: RDF, T](binder: URIBinder[Rdf, T]) = new URIBinderW[Rdf, T](binder)

  implicit class URIBinderW2[Rdf <: RDF, T](val binder: URIBinder[Rdf, T]) extends AnyVal {
    def toNodeBinder(implicit ops: RDFOps[Rdf]): NodeBinder[Rdf, T] = URIBinder.toNodeBinder(ops, binder)
  }

  def toNodeBinder[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: URIBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Try[T] =
        ops.foldNode(node)(
          uri => binder.fromUri(uri),
          bnode => Failure(FailedConversion(node + " is a BNode, not a URI")),
          uri => Failure(FailedConversion(node + " is a Literal, not a URI"))
        )

      def toNode(t: T): Rdf#Node = binder.toUri(t)
    }

  class URIBinderForURI[Rdf <: RDF](val ops: RDFOps[Rdf]) extends AnyVal with URIBinder[Rdf, Rdf#URI] {
      def fromUri(uri: Rdf#URI): Try[Rdf#URI] = Success(uri)

      def toUri(t: Rdf#URI): Rdf#URI = t
  }

  implicit def uriBinderForURI[Rdf <: RDF](implicit ops: RDFOps[Rdf]): URIBinder[Rdf, Rdf#URI] =
    new URIBinderForURI(ops)

}

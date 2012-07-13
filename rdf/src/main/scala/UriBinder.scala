package org.w3.banana

import scalaz._

trait ToURI[Rdf <: RDF, T] {
  def toUri(t: T): Rdf#URI
}

trait FromURI[Rdf <: RDF, T] {
  def fromUri(uri: Rdf#URI): Validation[BananaException, T]
}

trait URIBinder[Rdf <: RDF, T] extends FromURI[Rdf, T] with ToURI[Rdf, T]

class URIBinderW[Rdf <: RDF, T](binder: URIBinder[Rdf, T]) {
  def uriMaker[C](f: C => T): ToURI[Rdf, C] = new ToURI[Rdf, C] {
    def toUri(c: C): Rdf#URI = binder.toUri(f(c))
  }
}

object URIBinder {

  implicit def toURIBinderW[Rdf <: RDF, T](binder: URIBinder[Rdf, T]) = new URIBinderW[Rdf, T](binder)

  def toNodeBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: URIBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Validation[BananaException, T] =
        ops.foldNode(node)(
          uri => binder.fromUri(uri),
          bnode => Failure(FailedConversion(node + " is a BNode, not a URI")),
          uri => Failure(FailedConversion(node + " is a Literal, not a URI"))
        )

      def toNode(t: T): Rdf#Node = binder.toUri(t)
    }

  def naturalBinder[Rdf <: RDF](implicit ops: RDFOperations[Rdf]): URIBinder[Rdf, Rdf#URI] =
    new URIBinder[Rdf, Rdf#URI] {

      def fromUri(uri: Rdf#URI): Validation[BananaException, Rdf#URI] = Success(uri)

      def toUri(t: Rdf#URI): Rdf#URI = t

    }

}

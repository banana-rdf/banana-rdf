package org.w3.banana

import scalaz._

trait URIBinder[Rdf <: RDF, T] {
  def fromUri(uri: Rdf#URI): Validation[BananaException, T]
  def toUri(t: T): Rdf#URI
}

object URIBinder {

  def toNodeBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: URIBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Validation[BananaException, T] =
        ops.Node.fold(node)(
          uri => binder.fromUri(uri),
          bnode => Failure(FailedConversion(node + " is a BNode, not a URI")),
          uri => Failure(FailedConversion(node + " is a Literal, not a URI"))
        )

      def toNode(t: T): Rdf#Node = binder.toUri(t)
    }

}

package org.w3.banana.binder

import org.w3.banana._

import scala.util._

trait URIBinder[Rdf <: RDF, T] extends FromURI[Rdf, T] with ToURI[Rdf, T]

object URIBinder {

  implicit def FromURIToURI2URIBinder[Rdf <: RDF, T](implicit from: FromURI[Rdf, T], to: ToURI[Rdf, T]): URIBinder[Rdf, T] =
    new URIBinder[Rdf, T] {
      def fromURI(uri: Rdf#URI): Try[T] = from.fromURI(uri)
      def toURI(t: T): Rdf#URI = to.toURI(t)
    }

}

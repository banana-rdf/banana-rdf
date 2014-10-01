package org.w3.banana.binder

import org.w3.banana._

import scala.util._

trait FromURI[Rdf <: RDF, +T] {
  def fromURI(uri: Rdf#URI): Try[T]
}

object FromURI {

  implicit def URIFromURI[Rdf <: RDF] = new FromURI[Rdf, Rdf#URI] {
    def fromURI(uri: Rdf#URI): Try[Rdf#URI] = Success(uri)
  }

}

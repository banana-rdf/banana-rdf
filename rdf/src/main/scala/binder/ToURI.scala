package org.w3.banana.binder

import org.w3.banana._

trait ToURI[Rdf <: RDF, -T] {
  def toURI(t: T): Rdf#URI
}

object ToURI {

  implicit def URIToURI[Rdf <: RDF] = new ToURI[Rdf, Rdf#URI] {
    def toURI(t: Rdf#URI): Rdf#URI = t
  }

}

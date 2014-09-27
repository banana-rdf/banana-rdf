package org.w3.banana.binder

import org.w3.banana._

trait ToURI[Rdf <: RDF, -T] {
  def toURI(t: T): Rdf#URI
}

object ToURI {

  implicit def URIToURI[Rdf <: RDF] = new ToURI[Rdf, Rdf#URI] {
    def toURI(t: Rdf#URI): Rdf#URI = t
  }

  implicit def javaURLToURI[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new ToURI[Rdf, java.net.URL] {
    def toURI(t: java.net.URL): Rdf#URI = ops.URI(t.toString)
  }

  implicit def javaURIToURI[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new ToURI[Rdf, java.net.URI] {
    def toURI(t: java.net.URI): Rdf#URI = ops.URI(t.toString)
  }

}

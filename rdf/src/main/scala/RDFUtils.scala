package org.w3.banana

object RDFUtils {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): RDFUtils[Rdf] = new RDFUtilsBuilder[Rdf](ops)
}

trait RDFUtils[Rdf <: RDF] { self =>

  def supportDocument(iri: Rdf#URI): Rdf#URI

  class URIW(iri: Rdf#URI) {
    def supportDocument: Rdf#URI =
      self.supportDocument(iri)
  }

  implicit def wrapURI(iri: Rdf#URI) = new URIW(iri)
}

class RDFUtilsBuilder[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends RDFUtils[Rdf] {

  import ops._

  def supportDocument(iri: Rdf#URI): Rdf#URI = {
    val URI(iriString) = iri
    val uri = new java.net.URI(iriString)
    import uri._
    val uriNoFrag = new java.net.URI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
    URI(uriNoFrag.toString)
  }

}

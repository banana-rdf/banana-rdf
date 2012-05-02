package org.w3.rdf

object RDFUtils {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): RDFUtils[Rdf] = new RDFUtilsBuilder[Rdf](ops)
}

trait RDFUtils[Rdf <: RDF] { self =>

  def supportDocument(iri: Rdf#IRI): Rdf#IRI

  class IRIW(iri: Rdf#IRI) {
    def supportDocument: Rdf#IRI =
      self.supportDocument(iri)
  }

  implicit def wrapIRI(iri: Rdf#IRI) = new IRIW(iri)
}

class RDFUtilsBuilder[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends RDFUtils[Rdf] {

  import ops._

  def supportDocument(iri: Rdf#IRI): Rdf#IRI = {
    val IRI(iriString) = iri
    val uri = new java.net.URI(iriString)
    import uri._
    val uriNoFrag = new java.net.URI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
    IRI(uriNoFrag.toString)
  }

}

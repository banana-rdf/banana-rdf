package org.w3.banana

trait RDFWriter[Rdf <: RDF, +T] extends Writer[Rdf#Graph, T]

object RDFWriter {

  def apply[Rdf <: RDF, T](implicit writer: RDFWriter[Rdf, T]): RDFWriter[Rdf, T] = writer

}

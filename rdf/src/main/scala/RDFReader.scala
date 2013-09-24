package org.w3.banana

import java.io._
import scala.util._

object RDFReader {

  def apply[Rdf <: RDF, S](implicit rdfReader: RDFReader[Rdf, S]): RDFReader[Rdf, S] = rdfReader

}

trait RDFReader[Rdf <: RDF, +S] {

  def syntax: Syntax[S]

  def read(is: InputStream, base: String): Try[Rdf#Graph]

  def read(input: String, base: String): Try[Rdf#Graph] = {
    val is = new ByteArrayInputStream(input.getBytes("UTF-8"))
    read(is, base)
  }

}

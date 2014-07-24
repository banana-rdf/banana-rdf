package org.w3.banana

import java.io._
import scala.concurrent.Future
import scala.util._

object RDFReader {

  def apply[Rdf <: RDF, S](implicit rdfReader: RDFReader[Rdf, S]): RDFReader[Rdf, S] = rdfReader

}

trait RDFReader[Rdf <: RDF, +S] {

  def syntax: Syntax[S]

  def read(is: String, base: String): Future[Rdf#Graph]

}

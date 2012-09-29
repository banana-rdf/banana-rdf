package org.w3.banana

import java.io._
import scalax.io._
import scalaz.Validation
import scalaz.Validation._

object RDFReader {

  def apply[Rdf <: RDF, S](implicit rdfReader: RDFReader[Rdf, S]): RDFReader[Rdf, S] = rdfReader

}

trait RDFReader[Rdf <: RDF, +S] {

  def syntax: Syntax[S]

  def read[R <: Reader](resource: ReadCharsResource[R], base: String): BananaValidation[Rdf#Graph]

  def read[R](inputResource: InputResource[R], base: String): BananaValidation[Rdf#Graph] =
    read(inputResource.reader(Codec.UTF8), base)

  def read(input: String, base: String): BananaValidation[Rdf#Graph] = {
    val reader = new StringReader(input)
    read(Resource.fromReader(reader), base)
  }

}

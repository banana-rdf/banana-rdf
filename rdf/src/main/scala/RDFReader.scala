package org.w3.banana

import java.io._
import scalaz.Validation
import scalaz.Validation._

object RDFReader {

  def apply[Rdf <: RDF, S](implicit rdfReader: RDFReader[Rdf, S]): RDFReader[Rdf, S] = rdfReader

}

trait RDFReader[Rdf <: RDF, +S] {

  def syntax: Syntax[S]

  def read(is: InputStream, base: String): BananaValidation[Rdf#Graph]

  def read(reader: java.io.Reader, base: String): BananaValidation[Rdf#Graph]

  def read(file: File, base: String): BananaValidation[Rdf#Graph] =
    for {
      fis <- WrappedThrowable.fromTryCatch { new BufferedInputStream(new FileInputStream(file)) }
      graph <- read(fis, base)
    } yield graph

  def read(file: File, base: String, encoding: String): BananaValidation[Rdf#Graph] =
    for {
      fis <- WrappedThrowable.fromTryCatch { new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding) }
      graph <- read(fis, base)
    } yield graph

  def read(s: String, base: String): BananaValidation[Rdf#Graph] = {
    val reader = new StringReader(s)
    read(reader, base)
  }

}

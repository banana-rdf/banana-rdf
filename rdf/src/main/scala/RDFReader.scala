package org.w3.banana

import java.io._

import scalaz.Validation
import scalaz.Validation._

trait RDFReader[Rdf <: RDF, +Language] {

  def read(is: InputStream, base: String): Validation[BananaException, Rdf#Graph]

  def read(reader: Reader, base: String): Validation[BananaException, Rdf#Graph]

  def read(file: File, base: String): Validation[BananaException, Rdf#Graph] =
    for {
      fis <- WrappedThrowable.fromTryCatch { new BufferedInputStream(new FileInputStream(file)) }
      graph <- read(fis, base)
    } yield graph

  def read(file: File, base: String, encoding: String): Validation[BananaException, Rdf#Graph] =
    for {
      fis <- WrappedThrowable.fromTryCatch { new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding) }
      graph <- read(fis, base)
    } yield graph

  def read(s: String, base: String): Validation[BananaException, Rdf#Graph] = {
    val reader = new StringReader(s)
    read(reader, base)
  }

}

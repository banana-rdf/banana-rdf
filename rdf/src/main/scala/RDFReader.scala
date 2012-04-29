package org.w3.rdf

import java.io._

import scalaz.Validation
import scalaz.Validation._

trait RDFReader[Rdf <: RDF, Serialization <: RDFSerialization] {
  
  def read(is: InputStream, base: String): Validation[Throwable, Rdf#Graph]
  
  def read(reader: Reader, base: String): Validation[Throwable, Rdf#Graph]
  
  def read(file: File, base: String): Validation[Throwable, Rdf#Graph] =
    for {
      fis <- fromTryCatch { new BufferedInputStream(new FileInputStream(file)) }
      graph <- read(fis, base)
    } yield graph
  
  def read(file: File, base: String, encoding: String): Validation[Throwable, Rdf#Graph] =
    for {
      fis <- fromTryCatch { new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), encoding) }
      graph <- read(fis, base)
    } yield graph
  
  def read(s: String, base: String): Validation[Throwable, Rdf#Graph] = {
    val reader = new StringReader(s)
    read(reader, base)
  }
  
}

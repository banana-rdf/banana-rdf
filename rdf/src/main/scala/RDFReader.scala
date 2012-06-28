package org.w3.banana

import java.io._
import scalaz.Validation
import scalaz.Validation._

/**
 * typeclass for an RDF BlockingReader that returns Graphs
 * (probably should move the other methods down to BlockingReader!)
 *
 * @tparam Rdf
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                    for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait RDFReader[Rdf <: RDF, SyntaxType] extends BlockingReader[Rdf#Graph,SyntaxType] {

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


trait BlockingReader[Result, +SyntaxType] {
  def read(is: InputStream, base: String): Validation[BananaException, Result]

  def read(reader: java.io.Reader, base: String): Validation[BananaException, Result]

}
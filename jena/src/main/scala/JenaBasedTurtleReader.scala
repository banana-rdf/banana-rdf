package org.w3.banana.jena.util

import org.w3.banana._
import org.w3.banana.jena._
import java.io._

import scalaz.Validation

/**
 * a Jena based default Turtle reader
 *
 * The given graph is transformed into the Jena world using a Transformer
 */
class JenaBasedTurtleReader[Rdf <: RDF](val ops: RDFOperations[Rdf])
extends RDFReader[Rdf, Turtle] {
  
  private val jenaToM = new RDFTransformer[Jena, Rdf](JenaOperations, ops)
  
  def read(is: InputStream, base: String): Validation[Throwable, Rdf#Graph] =
    JenaTurtleReader.read(is, base) map jenaToM.transform
  
  def read(reader: Reader, base: String): Validation[Throwable, Rdf#Graph] =
    JenaTurtleReader.read(reader, base) map jenaToM.transform
  
}

object JenaBasedTurtleReader {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): JenaBasedTurtleReader[Rdf] =
    new JenaBasedTurtleReader[Rdf](ops)
}

package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._
import java.io._

import scalaz.Validation

class DefaultTurtleReader[Rdf <: RDF](val ops: RDFOperations[Rdf])
extends RDFReader[Rdf, Turtle] {
  
  private val jenaToM = new RDFTransformer[Jena, Rdf](JenaOperations, ops)
  
  def read(is: InputStream, base: String): Validation[Throwable, Rdf#Graph] =
    JenaTurtleReader.read(is, base) map jenaToM.transform
  
  def read(reader: Reader, base: String): Validation[Throwable, Rdf#Graph] =
    JenaTurtleReader.read(reader, base) map jenaToM.transform
  
}
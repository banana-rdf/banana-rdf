package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._
import java.io._

class DefaultTurtleReader[Rdf <: RDF](override val ops: RDFOperations[Rdf])
extends TurtleReader[Rdf](ops) {
  
  private val jenaToM = new RDFTransformer[JenaDataType, Rdf](JenaOperations, ops)
  
  def read(is: InputStream, base: String): Either[Throwable, Rdf#Graph] =
    JenaTurtleReader.read(is, base).right.map(jenaToM.transform)
  
  def read(reader: Reader, base: String): Either[Throwable, Rdf#Graph] =
    JenaTurtleReader.read(reader, base).right.map(jenaToM.transform)
  
}
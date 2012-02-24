package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._
import java.io._

class DefaultTurtleReader[RDF <: RDFDataType](override val ops: RDFOperations[RDF])
extends TurtleReader[RDF](ops) {
  
  private val jenaToM = new Transformer[JenaDataType, RDF](JenaOperations, ops)
  
  def read(is: InputStream, base: String): Either[Throwable, RDF#Graph] =
    JenaTurtleReader.read(is, base).right.map(jenaToM.transform)
  
  def read(reader: Reader, base: String): Either[Throwable, RDF#Graph] =
    JenaTurtleReader.read(reader, base).right.map(jenaToM.transform)
  
}
package org.w3.banana

import java.io._

import scalaz.Validation
import scalaz.Validation._

trait RDFReaderFactory[Rdf <: RDF] {

  def find(ser: RDFSerialization): Option[RDFReader[Rdf,_]] = ser match {
    case RDFXML => Some(RDFXMLReader)
    case Turtle => Some(TurtleReader)
    case _ => None
  }

  def RDFXMLReader: RDFReader[Rdf, RDFXML]
  def TurtleReader: RDFReader[Rdf, Turtle]
}
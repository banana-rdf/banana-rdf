package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._
import java.io._

class DefaultTurtleReader[M <: RDFModule](override val m: M) extends TurtleReader(m) {
  
  private val jenaToM = new Transformer[JenaModule.type, m.type](JenaModule, m)
  
  def read(is: InputStream, base: String): Either[Throwable, m.Graph] =
    JenaTurtleReader.read(is, base).right.map(jenaToM.transform)
  
  def read(reader: Reader, base: String): Either[Throwable, m.Graph] =
    JenaTurtleReader.read(reader, base).right.map(jenaToM.transform)
  
}
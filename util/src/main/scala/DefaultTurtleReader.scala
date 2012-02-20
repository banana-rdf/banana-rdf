package org.w3.rdf.util

import org.w3.rdf
import org.w3.rdf.{RDFModule, Transformer}
import org.w3.rdf.jena
import org.w3.rdf.jena.JenaModule
import java.io._

class DefaultTurtleReader[M <: RDFModule](override val m: M) extends rdf.TurtleReader(m) {
  
  private val jenaToM = new Transformer[JenaModule.type, m.type](JenaModule, m)
  
  def read(is: InputStream, base: String): Either[Throwable, m.Graph] =
    jena.TurtleReader.read(is, base).right.map(jenaToM.transform)
  
  def read(reader: Reader, base: String): Either[Throwable, m.Graph] =
    jena.TurtleReader.read(reader, base).right.map(jenaToM.transform)
  
}
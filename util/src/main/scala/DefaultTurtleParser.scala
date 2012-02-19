package org.w3.rdf.util

import org.w3.rdf
import org.w3.rdf.{RDFModule, Transformer}
import org.w3.rdf.jena
import org.w3.rdf.jena.JenaModule
import java.io._

class DefaultTurtleParser[M <: RDFModule](override val m: M) extends rdf.TurtleParser(m) {
  
  private val jenaToM = new Transformer[JenaModule.type, m.type](JenaModule, m)
  
  def read(is: InputStream, base: String): m.Graph =
    jenaToM.transform(jena.TurtleParser.read(is, base))
  
  def read(reader: Reader, base: String): m.Graph =
   jenaToM.transform(jena.TurtleParser.read(reader, base))
  
}
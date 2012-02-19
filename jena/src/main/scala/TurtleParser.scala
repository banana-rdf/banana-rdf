package org.w3.rdf.jena

import org.w3.rdf
import java.io._
import com.hp.hpl.jena.rdf.model._

object TurtleParser extends rdf.TurtleParser(JenaModule) {
  
  import JenaModule._
  
  def read(is: InputStream, base: String): Graph = {
    val model = ModelFactory.createDefaultModel()
    model.getReader("TURTLE").read(model, is, base)
    Graph.fromJena(model.getGraph)
  }
  
  def read(reader: Reader, base: String): Graph = {
    val model = ModelFactory.createDefaultModel()
    model.getReader("TURTLE").read(model, reader, base)
    Graph.fromJena(model.getGraph)
  }
  
  
}
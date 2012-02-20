package org.w3.rdf.jena

import org.w3.rdf
import java.io._
import com.hp.hpl.jena.rdf.model._

object TurtleReader extends rdf.TurtleReader(JenaModule) {
  
  import JenaModule._
  
  def read(is: InputStream, base: String): Either[Throwable, Graph] =
    try {
      val model = ModelFactory.createDefaultModel()
      model.getReader("TURTLE").read(model, is, base)
      val graph = Graph.fromJena(model.getGraph)
      Right(graph)
    } catch {
      case t => Left(t)
    }
  
  def read(reader: Reader, base: String): Either[Throwable, Graph] =
    try {
      val model = ModelFactory.createDefaultModel()
      model.getReader("TURTLE").read(model, reader, base)
      val graph = Graph.fromJena(model.getGraph)
      Right(graph)
    } catch {
      case t => Left(t)
    }
  
  
}
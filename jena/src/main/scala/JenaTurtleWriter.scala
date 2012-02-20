package org.w3.rdf.jena

import org.w3.rdf
import java.io._
import com.hp.hpl.jena.rdf.model._

object JenaTurtleWriter extends rdf.TurtleWriter(JenaModule) {
  
  import JenaModule._
  
  def write(graph: Graph, os: OutputStream, base: String): Either[Throwable, Unit] =
    try {
      val model = ModelFactory.createModelForGraph(graph.jenaGraph)
      model.getWriter("TURTLE").write(model, os, base)
      Right()
    } catch {
      case t => Left(t)
    }
  
  def write(graph: Graph, writer: Writer, base: String): Either[Throwable, Unit] =
    try {
      val model = ModelFactory.createModelForGraph(graph.jenaGraph)
      model.getWriter("TURTLE").write(model, writer, base)
      Right()
    } catch {
      case t => Left(t)
    }
  
  
}
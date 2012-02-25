package org.w3.rdf.jena

import org.w3.rdf._
import java.io._
import com.hp.hpl.jena.rdf.model._

object JenaTurtleWriter extends TurtleWriter[Jena](JenaOperations) {
  
  import JenaOperations._
  
  def write(graph: Jena#Graph, os: OutputStream, base: String): Either[Throwable, Unit] =
    try {
      val model = ModelFactory.createModelForGraph(graph)
      model.getWriter("TURTLE").write(model, os, base)
      Right()
    } catch {
      case t => Left(t)
    }
  
  def write(graph: Jena#Graph, writer: Writer, base: String): Either[Throwable, Unit] =
    try {
      val model = ModelFactory.createModelForGraph(graph)
      model.getWriter("TURTLE").write(model, writer, base)
      Right()
    } catch {
      case t => Left(t)
    }
  
  
}
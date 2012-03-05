package org.w3.rdf.jena

import org.w3.rdf._
import java.io._
import com.hp.hpl.jena.rdf.model._

import scalaz.Validation
import scalaz.Validation._

object JenaTurtleWriter extends TurtleWriter[Jena](JenaOperations) {
  
  import JenaOperations._
  
  def write(graph: Jena#Graph, os: OutputStream, base: String): Validation[Throwable, Unit] = fromTryCatch {
    val model = ModelFactory.createModelForGraph(graph)
    model.getWriter("TURTLE").write(model, os, base)
  }
  
  def write(graph: Jena#Graph, writer: Writer, base: String): Validation[Throwable, Unit] = fromTryCatch {
    val model = ModelFactory.createModelForGraph(graph)
    model.getWriter("TURTLE").write(model, writer, base)
  }
  
  
}
package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model._

import scalaz.Validation
import scalaz.Validation._

class JenaWriter(syntax: String) extends BlockingWriter[Jena] {
  val ops = JenaOperations
  import JenaOperations._
  
  def write(graph: Jena#Graph, os: OutputStream, base: String): Validation[BananaException, Unit] = WrappedThrowable.fromTryCatch {
    val model = ModelFactory.createModelForGraph(graph)
    model.getWriter(syntax).write(model, os, base)
  }
  
  def write(graph: Jena#Graph, writer: Writer, base: String): Validation[BananaException, Unit] = WrappedThrowable.fromTryCatch {
    val model = ModelFactory.createModelForGraph(graph)
    model.getWriter(syntax).write(model, writer, base)
  }
}

object JenaTurtleWriter extends JenaWriter("TURTLE") with TurtleWriter[Jena]

object JenaRdfXmlWriter extends JenaWriter("RDF/XML") with RdfXmlWriter[Jena]

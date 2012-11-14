package org.w3.banana.plantain

import org.w3.banana._
import org.openrdf.model._
import java.io._
import scalax.io._
import scala.util._

class Collector extends org.openrdf.rio.RDFHandler {

  var graph: Graph = Graph.empty

  def startRDF(): Unit = ()

  def endRDF(): Unit = ()

  def handleComment(comment: String): Unit = ()

  def handleNamespace(prefix: String, uri: String): Unit = ()

  def handleStatement(statement: Statement): Unit =
    graph += Triple.fromSesame(statement)

}

object PlantainTurtleReader extends RDFReader[Plantain, Turtle] {

  val syntax = Syntax[Turtle]

  def read[R <: Reader](resource: ReadCharsResource[R], base: String): Try[Plantain#Graph] = Try {
    resource acquireAndGet { reader => 
      val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
      val collector = new Collector
      turtleParser.setRDFHandler(collector)
      turtleParser.parse(reader, base)
      collector.graph
    }
  }

}

object PlantainRDFXMLReader extends RDFReader[Plantain, RDFXML] {

  val syntax = Syntax[RDFXML]

  def read[R <: Reader](resource: ReadCharsResource[R], base: String): Try[Plantain#Graph] = Try {
    resource acquireAndGet { reader =>
      val parser = new org.openrdf.rio.rdfxml.RDFXMLParser
      val collector = new Collector
      parser.setRDFHandler(collector)
      parser.parse(reader, base)
      collector.graph
    }
  }

}

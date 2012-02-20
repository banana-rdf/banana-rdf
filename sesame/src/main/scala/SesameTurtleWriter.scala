package org.w3.rdf.sesame

import org.w3.rdf._
import java.io._
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}

object SesameTurtleWriter extends TurtleWriter(SesameModule) {
  
  import SesameModule._
  
  private def write(graph: Graph, turtleWriter: STurtleWriter, base: String): Either[Throwable, Unit] =
    try {
      turtleWriter.startRDF()
      graph foreach turtleWriter.handleStatement
      turtleWriter.endRDF()
      Right()
    } catch {
      case t => Left(t)
    }
  
  def write(graph: Graph, os: OutputStream, base: String): Either[Throwable, Unit] = {
    val turtleWriter = new STurtleWriter(os)
    write(graph, turtleWriter, base)
  }
  
  def write(graph: Graph, writer: Writer, base: String): Either[Throwable, Unit] = {
    val turtleWriter = new STurtleWriter(writer)
    write(graph, turtleWriter, base)
  }
  
}
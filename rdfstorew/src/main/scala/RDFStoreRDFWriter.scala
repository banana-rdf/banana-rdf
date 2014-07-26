package org.w3.banana.rdfstorew

import org.w3.banana.{Syntax, Turtle, RDFWriterSelector, RDFWriter}

import scala.util.Try
import java.io.OutputStream


object RDFStoreTurtleWriter extends RDFWriter[RDFStore, Turtle] {

  val syntax: Syntax[Turtle] = Syntax.Turtle


  def write(graph: RDFStore#Graph, base: String): Try[String] = Try {
    graph.graph.toNT().asInstanceOf[String]
  }

  override def write(obj: RDFStore#Graph, outputstream: OutputStream, base: String) = ???
}

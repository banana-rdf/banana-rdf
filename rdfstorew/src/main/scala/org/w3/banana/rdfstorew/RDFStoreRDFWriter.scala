package org.w3.banana.rdfstorew

import java.io.OutputStream

import org.w3.banana.{ RDFWriter, Syntax, Turtle }

import scala.util.Try

object RDFStoreTurtleWriter extends RDFWriter[RDFStore, Turtle] {

  val syntax: Syntax[Turtle] = Syntax.Turtle

  def asString(graph: RDFStore#Graph, base: String): Try[String] = Try {
    graph.graph.toNT().asInstanceOf[String]
  }

  override def write(obj: RDFStore#Graph, outputstream: OutputStream, base: String) = ???
}

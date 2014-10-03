package org.w3.banana.jena

import java.io.{ Writer => jWriter, _ }

import org.apache.jena.riot._
import org.w3.banana._

import scala.util._

/**
 * Create an RDF Writer using Jena's serialisers
 */
object JenaRDFWriter {

  def makeRDFWriter[S](lang: Lang)(implicit _syntax: Syntax[S]): RDFWriter[Jena, S] = new RDFWriter[Jena, S] {
    val syntax = _syntax
    def write(graph: Jena#Graph, os: OutputStream, base: String): Try[Unit] = Try {
      import org.w3.banana.jena.Jena.ops._
      val relativeGraph = graph.relativize(URI(base))
      RDFDataMgr.write(os, relativeGraph, lang)
    }

    def asString(graph: Jena#Graph, base: String): Try[String] = Try {
      val result = new StringWriter()
      import org.w3.banana.jena.Jena.ops._
      val relativeGraph = graph.relativize(URI(base))
      RDFDataMgr.write(result, relativeGraph, lang)
      result.toString()
    }
  }

  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = makeRDFWriter[RDFXML](Lang.RDFXML)

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = makeRDFWriter[Turtle](Lang.TURTLE)

  implicit val n3Writer: RDFWriter[Jena, N3] = makeRDFWriter[N3](Lang.N3)

  val selector: RDFWriterSelector[Jena] =
    RDFWriterSelector[Jena, RDFXML] combineWith RDFWriterSelector[Jena, Turtle]

}

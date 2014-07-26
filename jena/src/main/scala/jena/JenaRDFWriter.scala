package org.w3.banana.jena

import org.w3.banana._
import java.io.{ Writer => jWriter }
import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.util._
import java.io._
import org.apache.jena.riot._

/**
 * Create an RDF Writer using Jena's serialisers
 */
object JenaRDFWriter {

  def makeRDFWriter[S](lang: Lang)(implicit _syntax: Syntax[S]): RDFWriter[Jena, S] = new RDFWriter[Jena, S] {
    val syntax = _syntax
    def write(graph: Jena#Graph, os: OutputStream, base: String): Try[Unit] = Try {
      import Jena.Ops._
      val relativeGraph = graph.relativize(URI(base))
      RDFDataMgr.write(os, relativeGraph, lang)
    }

    override def write(obj: Jena#Graph, base: String) = ???
  }

  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = makeRDFWriter[RDFXML](Lang.RDFXML)

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = makeRDFWriter[Turtle](Lang.TURTLE)

  val selector: RDFWriterSelector[Jena] =
    RDFWriterSelector[Jena, RDFXML] combineWith RDFWriterSelector[Jena, Turtle]

}

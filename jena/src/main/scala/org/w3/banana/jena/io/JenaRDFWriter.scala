package org.w3.banana.jena
package io

import java.io.{ Writer => jWriter, _ }

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl
import com.hp.hpl.jena.rdfxml.xmloutput.impl.Abbreviated
import org.apache.jena.riot.{ Lang => JenaLang, _ }
import org.w3.banana._
import org.w3.banana.io._

import scala.util._
import org.w3.banana.jena.Jena.ops._

/** Helpers to create Jena writers. */
object JenaRDFWriter {

  private [JenaRDFWriter] val writerFactory = new RDFWriterFImpl()

  private [JenaRDFWriter] def makeRDFWriter[S](lang: JenaLang)(implicit _syntax:  Syntax[S]): RDFWriter[Jena, Try, S] = new RDFWriter[Jena, Try, S] {

    def write(graph: Jena#Graph, os: OutputStream, base: String): Try[Unit] = Try {
      val model = ModelFactory.createModelForGraph(graph)
      writerFactory.getWriter(lang.getLabel).write(model, os, base)
    }

    def asString(graph: Jena#Graph, base: String): Try[String] = Try {
      val result = new StringWriter()
      val model = ModelFactory.createModelForGraph(graph)
      writerFactory.getWriter(lang.getLabel).write(model, result, base)
      result.toString()
    }
  }

  implicit val rdfxmlWriter: RDFWriter[Jena, Try, RDFXML] = new RDFWriter[Jena, Try, RDFXML] {
    def write(graph: Jena#Graph, os: OutputStream, base: String): Try[Unit] = Try {
      val writer = new Abbreviated()
      writer.setProperty("relativeURIs", "same-document,relative")
      val model = ModelFactory.createModelForGraph(graph)
      writer.write(model, os, base)
    }

    def asString(graph: Jena#Graph, base: String): Try[String] = Try {
      val result = new StringWriter()
      val writer = new Abbreviated()
      writer.setProperty("relativeURIs", "same-document,relative")
      val model = ModelFactory.createModelForGraph(graph)
      writer.write(model, result, base)
      result.toString()
    }
  }

  implicit val turtleWriter: RDFWriter[Jena, Try, Turtle] = new RDFWriter[Jena, Try, Turtle] {
    // with the turtle writer we pass it  relative graph as that seems to stop the parser from adding the
    // @base statement at the top!
    def write(graph: Jena#Graph, os: OutputStream, base: String): Try[Unit] = Try {
      val relativeGraph = graph.relativize(URI(base))
      RDFDataMgr.write(os, relativeGraph, JenaLang.TURTLE)
    }

    def asString(graph: Jena#Graph, base: String): Try[String] = Try {
      val result = new StringWriter()
      val relativeGraph = graph.relativize(URI(base))
      RDFDataMgr.write(result, relativeGraph, JenaLang.TURTLE)
      result.toString()
    }
  }

  implicit val n3Writer: RDFWriter[Jena, Try, N3] = makeRDFWriter[N3](RDFLanguages.N3)

  val selector: RDFWriterSelector[Jena, Try] =
    RDFWriterSelector[Jena, Try, RDFXML] combineWith RDFWriterSelector[Jena, Try, Turtle]

}

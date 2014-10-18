
package org.w3.banana.plantain
package io

import java.io.{ Reader, InputStream }

import akka.http.model.Uri
import org.openrdf.rio._
import org.openrdf.rio.turtle._
import org.openrdf.{ model => sesame }
import org.w3.banana.io._

import scala.util.Try

object PlantainTurtleReader extends RDFReader[Plantain, Turtle] {

  class Sink(var graph: model.Graph[Uri] = PlantainOps.emptyGraph, var prefixes: Map[String, String] = Map.empty) extends RDFHandler {

    def startRDF(): Unit = ()
    def endRDF(): Unit = ()
    def handleComment(comment: String): Unit = ()
    def handleNamespace(prefix: String, uri: String): Unit = prefixes += (prefix -> uri)
    def handleStatement(statement: org.openrdf.model.Statement): Unit = {
      val s: model.Node = statement.getSubject match {
        case bnode: sesame.BNode => model.BNode(bnode.getID)
        case uri: sesame.URI => model.URI(Uri(uri.toString))
      }
      val p: model.URI[Uri] = statement.getPredicate match {
        case uri: sesame.URI => model.URI(Uri(uri.toString))
      }
      val o: model.Node = statement.getObject match {
        case bnode: sesame.BNode => model.BNode(bnode.getID)
        case uri: sesame.URI => model.URI(Uri(uri.toString))
        case literal: sesame.Literal => model.Literal(literal.stringValue, model.URI(Uri(literal.getDatatype.stringValue)), Option(literal.getLanguage))
      }
      this.graph = this.graph + (s, p, o)
    }

  }

  /**
   *
   * @param is InputStream
   * @param base Url to use to resolve relative URLs  ( as String ) //todo: why not as a RDF#URI ?
   * @return A Success[Graph] or a Failure
   */
  def read(is: InputStream, base: String): Try[Plantain#Graph] = Try {
    val sink = new Sink
    val parser = new TurtleParser
    parser.setRDFHandler(sink)
    parser.parse(is, base)
    sink.graph
  }

  /**
   * Parse from the Reader. Readers have already made the encoding decision, so there is none left here to make
   * @param reader
   * @param base
   * @return
   */
  override def read(reader: Reader, base: String): Try[Plantain#Graph] = Try {
    val sink = new Sink
    val parser = new TurtleParser
    parser.setRDFHandler(sink)
    parser.parse(reader, base)
    sink.graph
  }
}

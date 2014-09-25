package org.w3.banana.plantain

import java.io.InputStream

import org.openrdf.rio._
import org.openrdf.rio.turtle._
import org.openrdf.{ model => sesame }
import org.w3.banana._
import akka.http.model.Uri

import scala.util.Try

object PlantainTurtleReader extends RDFReader[Plantain, Turtle] {

  val syntax: Syntax[Turtle] = Syntax.Turtle

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
   * todo: this is the wrong way around. The reader taking an inputstream should
   * call the reader taking a string or better a StringReader ( but does not exist yet in scala-js)
   * @param is
   * @param base
   * @return
   */
  def read(is: InputStream, base: String): Try[Plantain#Graph] = Try {
    val sink = new Sink
    val parser = new TurtleParser
    parser.setRDFHandler(sink)
    parser.parse(is, base)
    sink.graph
  }

  def main(args: Array[String]): Unit = {
    val is = new java.io.FileInputStream("/home/betehess/projects/banana-rdf/rdf-test-suite/src/main/resources/card.ttl")
    read(is, "http://example.com/")
  }

}

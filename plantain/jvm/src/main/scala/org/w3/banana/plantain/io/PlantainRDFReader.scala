package org.w3.banana.plantain
package io

import java.io.{ Reader, InputStream }
import akka.http.scaladsl.model.Uri
import org.openrdf.rio._
import org.openrdf.rio.turtle._
import org.openrdf.{ model => sesame }
import org.w3.banana.io._
import scala.util.Try

import PlantainOps._

object PlantainTurtleReader extends RDFReader[Plantain, Try, Turtle] {

  final class Sink extends RDFHandler {

    var graph: Plantain#Graph = emptyGraph

    var prefixes: Map[String, String] = Map.empty

    def startRDF(): Unit = ()

    def endRDF(): Unit = ()

    def handleComment(comment: String): Unit = ()

    def handleNamespace(prefix: String, uri: String): Unit = prefixes += (prefix -> uri)

    def handleStatement(statement: org.openrdf.model.Statement): Unit = {
      val s: Plantain#Node = statement.getSubject match {
        case bnode: sesame.BNode => BNode(bnode.getID)
        case uri: sesame.URI     => URI(uri.toString)
      }
      val p: Plantain#URI = statement.getPredicate match {
        case uri: sesame.URI => URI(uri.toString)
      }
      val o: Plantain#Node = statement.getObject match {
        case bnode: sesame.BNode     => BNode(bnode.getID)
        case uri: sesame.URI         => URI(uri.toString)
        case literal: sesame.Literal => literal.getLanguage match {
          case null => makeLiteral(literal.stringValue, Uri(literal.getDatatype.toString))
          case lang => makeLangTaggedLiteral(literal.stringValue, lang)
        }
      }
      graph += (s, p, o)
    }

  }

  def read(is: InputStream, base: String): Try[Plantain#Graph] = Try {
    val sink = new Sink
    val parser = new TurtleParser
    parser.setRDFHandler(sink)
    parser.parse(is, base)
    sink.graph
  }

  def read(reader: Reader, base: String): Try[Plantain#Graph] = Try {
    val sink = new Sink
    val parser = new TurtleParser
    parser.setRDFHandler(sink)
    parser.parse(reader, base)
    sink.graph
  }

}

package org.w3.banana.plantain
package io

import java.io.{ Reader, InputStream }
import akka.http.scaladsl.model.Uri
import org.eclipse.rdf4j.rio._
import org.eclipse.rdf4j.rio.turtle._
import org.eclipse.rdf4j.{ model => rdf4j }
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

    def handleStatement(statement: org.eclipse.rdf4j.model.Statement): Unit = {
      import scala.compat.java8.OptionConverters._
      val s: Plantain#Node = statement.getSubject match {
        case bnode: rdf4j.BNode => BNode(bnode.getID)
        case uri: rdf4j.IRI     => URI(uri.toString)
      }
      val p: Plantain#URI = statement.getPredicate match {
        case uri: rdf4j.IRI => URI(uri.toString)
      }
      val o: Plantain#Node = statement.getObject match {
        case bnode: rdf4j.BNode     => BNode(bnode.getID)
        case uri: rdf4j.IRI         => URI(uri.toString)
        case literal: rdf4j.Literal => literal.getLanguage.asScala match {
          case None => makeLiteral(literal.stringValue, Uri(literal.getDatatype.toString))
          case Some(lang) => makeLangTaggedLiteral(literal.stringValue, lang)
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

package org.w3.banana.plantain

import org.w3.banana._
import java.io.OutputStream
import scala.util.Try
import org.openrdf.rio.turtle._
import org.openrdf.{ model => sesame }
import org.openrdf.model.impl._

object PlantainTurtleWriter extends RDFWriter[Plantain, Turtle] {

  val syntax: Syntax[Turtle] = Syntax.Turtle

  def statement(s: model.Node, p: model.URI, o: model.Node): sesame.Statement = {
    val subject: sesame.Resource = s match {
      case model.URI(uri)                 => new URIImpl(uri.toString)
      case model.BNode(label)             => new BNodeImpl(label)
      case literal@model.Literal(_, _, _) => throw new IllegalArgumentException(s"$literal was in subject position")
    }
    val predicate: sesame.URI = p match {
      case model.URI(uri) => new URIImpl(uri.toString)
    }
    val objectt: sesame.Value = o match {
      case model.URI(uri)                                   => new URIImpl(uri.toString)
      case model.BNode(label)                               => new BNodeImpl(label)
      case model.Literal(lexicalForm, model.URI(uri), None) => new LiteralImpl(lexicalForm, new URIImpl(uri.toString))
      case model.Literal(lexicalForm, _, Some(lang))        => new LiteralImpl(lexicalForm, lang)
    }
    new StatementImpl(subject, predicate, objectt)
  }

  def write(graph: Plantain#Graph, outputstream: OutputStream, base: String): Try[Unit] = Try {

    val writer = new TurtleWriter(outputstream)

    writer.startRDF()

    graph.spo foreach { case (s, pos) =>
      pos foreach { case (p, os) =>
        os foreach { o =>
          writer.handleStatement(statement(s, p, o))
        }
      }
    }

    writer.endRDF()
    
  }

  def main(args: Array[String]): Unit = {
    val is = new java.io.FileInputStream("/home/betehess/projects/banana-rdf/rdf-test-suite/src/main/resources/card.ttl")
    
    val graph = PlantainTurtleReader.read(is, "http://example.com/").get

    write(graph, System.out, "http://example.com/")

  }


}

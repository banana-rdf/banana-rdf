package org.w3.banana.plantain.io

import java.io.{ByteArrayOutputStream, OutputStream}
import java.net.{URI => jURI}

import org.openrdf.model.impl._
import org.openrdf.rio.turtle._
import org.openrdf.{model => sesame}
import org.w3.banana.io._
import org.w3.banana.plantain._

import scala.util.Try

object PlantainTurtleWriter extends RDFWriter[Plantain, Try, Turtle] {

  /** accepts relative URIs */
  class MyUri(uri: String) extends sesame.URI {
    def getLocalName(): String = ???
    def getNamespace(): String = ???
    def stringValue(): String = uri
    override def toString(): String = uri
  }

  class Writer(graph: Plantain#Graph, outputstream: OutputStream, base: String) {
    val baseUri = new jURI(base)

    object Uri {
      def unapply(node: Plantain#Node): Option[jURI] = node match {
        case uri: Plantain#URI => Some(baseUri.relativize(new jURI(uri.toString)))
        case _                 => None
      }
    }

    def statement(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): sesame.Statement = {
      val subject: sesame.Resource = s match {
        case Uri(uri)           => new MyUri(uri.toString)
        case model.BNode(label) => new BNodeImpl(label)
        case literal            => throw new IllegalArgumentException(s"$literal was in subject position")
      }
      val predicate: sesame.URI = p match {
        case uri: Plantain#URI => new MyUri(uri.toString())
      }
      val objectt: sesame.Value = o match {
        case uri: Plantain#URI   => new MyUri(uri.toString)
        case model.BNode(label)  => new BNodeImpl(label)
        case literal             => PlantainOps.fromLiteral(literal) match {
          case (lexicalForm, uri, None)     => new LiteralImpl(lexicalForm, new URIImpl(uri.toString))
          case (lexicalForm, _, Some(lang)) => new LiteralImpl(lexicalForm, lang)
        }
      }
      new StatementImpl(subject, predicate, objectt)
    }

    def write(): Try[Unit] = Try {
      val writer = new TurtleWriter(outputstream)
      writer.startRDF()
      graph.triples foreach {
        case (s, p, o) =>
          writer.handleStatement(statement(s, p, o))
      }
      writer.endRDF()
    }

  }

  def write(graph: Plantain#Graph, outputstream: OutputStream, base: String): Try[Unit] = {
    val writer = new Writer(graph, outputstream, base)
    writer.write()
  }

  def asString(graph: Plantain#Graph, base: String): Try[String] = Try {
    val result = new ByteArrayOutputStream()
    val writer = new Writer(graph, result, base)
    writer.write()
    new String(result.toByteArray, "UTF-8")
  }

}

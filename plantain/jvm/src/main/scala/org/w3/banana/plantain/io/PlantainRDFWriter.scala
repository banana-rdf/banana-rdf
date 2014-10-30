package org.w3.banana.plantain.io

import java.io.{ ByteArrayOutputStream, OutputStream }
import java.net.{ URI => jURI } //we use jURIs because the correctly work with relative Uris

import akka.http.model.Uri
import org.openrdf.model.impl._
import org.openrdf.rio.turtle._
import org.openrdf.{ model => sesame }
import org.w3.banana.plantain.Plantain
import org.w3.banana.plantain.model
import org.w3.banana.io._

import scala.util.Try

object PlantainTurtleWriter extends RDFWriter[Plantain, Try, Turtle] {

  /** accepts relative URIs */
  class MyUri(uri: String) extends sesame.URI {
    def getLocalName(): String = ???
    def getNamespace(): String = ???
    def stringValue(): String = uri.toString()
    override def toString(): String = uri.toString()
  }

  class Writer(graph: Plantain#Graph, outputstream: OutputStream, base: String) {
    val baseUri = new jURI(base)

    object Uri {
      def unapply(node: model.Node): Option[jURI] = node match {
        case model.URI(uri) =>
          Some(baseUri.relativize(new jURI(uri.toString)))
        case _ => None
      }
    }

    def statement(s: model.Node, p: model.URI[Uri], o: model.Node): sesame.Statement = {
      val subject: sesame.Resource = s match {
        case Uri(uri)                         => new MyUri(uri.toString)
        case model.BNode(label)               => new BNodeImpl(label)
        case literal @ model.Literal(_, _, _) => throw new IllegalArgumentException(s"$literal was in subject position")
      }
      val predicate: sesame.URI = p match {
        case model.URI(uri) => new MyUri(uri.toString())
      }
      val objectt: sesame.Value = o match {
        case Uri(uri)                                         => new MyUri(uri.toString)
        case model.BNode(label)                               => new BNodeImpl(label)
        case model.Literal(lexicalForm, model.URI(uri), None) => new LiteralImpl(lexicalForm, new URIImpl(uri.toString))
        case model.Literal(lexicalForm, _, Some(lang))        => new LiteralImpl(lexicalForm, lang)
      }
      new StatementImpl(subject, predicate, objectt)
    }

    def write(): Try[Unit] = Try {
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

  }

  def write(graph: Plantain#Graph, outputstream: OutputStream, base: String): Try[Unit] = {
    val writer = new Writer(graph, outputstream, base)
    writer.write()
  }

  def asString(graph: Plantain#Graph, base: String): Try[String] = Try {
    val result = new ByteArrayOutputStream()
    //todo: clearly this trasformation into a byte array and then back into a character string,
    //shows that working at the byte level is wrong.
    val writer = new Writer(graph, result, base)
    writer.write()
    new String(result.toByteArray, "UTF-8")
  }

}

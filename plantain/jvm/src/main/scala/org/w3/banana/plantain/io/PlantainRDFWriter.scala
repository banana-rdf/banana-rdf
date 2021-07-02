package org.w3.banana.plantain.io

import java.io.{ByteArrayOutputStream, OutputStream}
import java.net.{URI => jURI}

import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.model.impl._
import org.eclipse.rdf4j.rio.turtle._
import org.eclipse.rdf4j.{model => rdf4j}
import org.w3.banana.Prefix
import org.w3.banana.io._
import org.w3.banana.plantain._

import scala.util.Try

object PlantainTurtleWriter extends RDFWriter[Plantain, Try, Turtle] {

  val valueFactory: ValueFactory = SimpleValueFactory.getInstance()

  /** accepts relative URIs */
  class MyUri(uri: String) extends rdf4j.IRI {
    def getLocalName(): String = ???
    def getNamespace(): String = ???
    def stringValue(): String = uri
    override def toString(): String = uri
  }

  class Writer(graph: Plantain#Graph, outputstream: OutputStream, base: Option[String]) {
    val baseUri: Option[jURI] = base.map(b => new jURI(b))

    object Uri {
      def unapply(node: Plantain#Node): Option[jURI] = node match {
        case uri: Plantain#URI =>
          baseUri.map(bs=> bs.relativize(new jURI(uri.toString))).orElse(Some(new jURI(uri.toString)))
        case _                 => None
      }
    }

    def statement(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): rdf4j.Statement = {
      val subject: rdf4j.Resource = s match {
        case Uri(uri)           => new MyUri(uri.toString)
        case model.BNode(label) => valueFactory.createBNode(label)
        case literal            => throw new IllegalArgumentException(s"$literal was in subject position")
      }
      val predicate: rdf4j.IRI = p match {
        case uri: Plantain#URI => new MyUri(uri.toString())
      }
      val objectt: rdf4j.Value = o match {
        case uri: Plantain#URI   => new MyUri(uri.toString)
        case model.BNode(label)  => valueFactory.createBNode(label)
        case literal             => PlantainOps.fromLiteral(literal) match {
          case (lexicalForm, uri, None)     => valueFactory.createLiteral(lexicalForm, valueFactory.createIRI(uri.toString))
          case (lexicalForm, _, Some(lang)) => valueFactory.createLiteral(lexicalForm, lang)
        }
      }
      valueFactory.createStatement(subject, predicate, objectt)
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

  def write(graph: Plantain#Graph, outputstream: OutputStream, base: Option[String], prefixes: Set[Prefix[Plantain]]): Try[Unit] = {
    val writer = new Writer(graph, outputstream, base)
    writer.write()
  }

  def asString(graph: Plantain#Graph, base: Option[String], prefixes: Set[Prefix[Plantain]]): Try[String] = Try {
    val result = new ByteArrayOutputStream()
    val writer = new Writer(graph, result, base)
    writer.write()
    new String(result.toByteArray, "UTF-8")
  }

}

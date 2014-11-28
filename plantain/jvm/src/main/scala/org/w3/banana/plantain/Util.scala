package org.w3.banana.plantain

import java.io.{ ByteArrayOutputStream, OutputStream }
import java.net.{ URI => jURI } //we use jURIs because the correctly work with relative Uris
import akka.http.model.Uri
import org.openrdf.model.impl._
import org.openrdf.rio.turtle._
import org.openrdf.{ model => sesame }
import org.w3.banana.plantain._
import org.w3.banana.io._
import scala.util.Try


object Util {

  def toStatement(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): sesame.Statement = {
    val subject: sesame.Resource = s match {
      case uri: Uri                         => new URIImpl(uri.toString)
      case model.BNode(label)               => new BNodeImpl(label)
      case literal @ model.Literal(_, _, _) => throw new IllegalArgumentException(s"$literal was in subject position")
    }
    val predicate: sesame.URI = p match {
      case uri: Uri => new URIImpl(uri.toString)
    }
    val objectt: sesame.Value = o match {
      case uri: Uri                              => new URIImpl(uri.toString)
      case model.BNode(label)                    => new BNodeImpl(label)
      case model.Literal(lexicalForm, uri, null) => new LiteralImpl(lexicalForm, new URIImpl(uri.toString))
      case model.Literal(lexicalForm, _, lang)   => new LiteralImpl(lexicalForm, lang)
    }
    new StatementImpl(subject, predicate, objectt)
  }

  def toSesameGraph(graph: Plantain#Graph): sesame.Graph = {
    val g = new LinkedHashModel
    graph.triples.foreach { case (s, p, o) =>
      g.add(toStatement(s, p, o))
    }
    g
  }

}

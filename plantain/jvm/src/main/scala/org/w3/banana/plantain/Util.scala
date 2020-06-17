package org.w3.banana.plantain

import akka.http.scaladsl.model.Uri
import org.eclipse.rdf4j.model.impl._
import org.eclipse.rdf4j.{model => rdf4j}


object Util {

  def toStatement(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): rdf4j.Statement = {
    val subject: rdf4j.Resource = s match {
      case uri: Uri                         => new URIImpl(uri.toString)
      case model.BNode(label)               => new BNodeImpl(label)
      case literal @ model.Literal(_, _, _) => throw new IllegalArgumentException(s"$literal was in subject position")
    }
    val predicate: rdf4j.URI = p match {
      case uri: Uri => new URIImpl(uri.toString)
    }
    val objectt: rdf4j.Value = o match {
      case uri: Uri                              => new URIImpl(uri.toString)
      case model.BNode(label)                    => new BNodeImpl(label)
      case model.Literal(lexicalForm, uri, null) => new LiteralImpl(lexicalForm, new URIImpl(uri.toString))
      case model.Literal(lexicalForm, _, lang)   => new LiteralImpl(lexicalForm, lang)
    }
    new StatementImpl(subject, predicate, objectt)
  }

  def toRdf4jGraph(graph: Plantain#Graph): rdf4j.Model = {
    val g = new LinkedHashModel
    graph.triples.foreach { case (s, p, o) =>
      g.add(toStatement(s, p, o))
    }
    g
  }

}

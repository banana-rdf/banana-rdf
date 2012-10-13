package org.w3

import scala.util._
import scala.concurrent._
import scala.concurrent.util._

package object banana
extends BananaRDFWriterSelector
with BananaSparqlSolutionWriterSelector {

  type TripleMatch[Rdf <: RDF] = (Rdf#NodeMatch, Rdf#NodeMatch, Rdf#NodeMatch)

  type SparqlSolutionsWriter[Rdf <: RDF, +T] = Writer[Rdf#Solutions, T]

  type RDFWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Graph]

  type SparqlSolutionsWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Solutions]

  implicit class FutureW[T](f: Future[T]) {
    def getOrFail(): T = {
      Await.result(f, Duration("3s"))
    }
  }

  implicit class TryW[T](t: Try[T]) {
    def asFuture: Future[T] = t match {
      case Success(s) => Future.successful(s)
      case Failure(f) => Future.failed(f)
    }
  }

  implicit class AnyW[T](t: => T) {
    def asFuture: Future[T] =
      try {
        Future.successful(t)
      } catch { case e: Exception =>
        Future.failed(e)
      }
  }

  implicit def graphSyntax[Rdf <: RDF](graph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): syntax.GraphSyntax[Rdf] = new syntax.GraphSyntax[Rdf](graph)

  implicit def nodeSyntax[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): syntax.NodeSyntax[Rdf] = new syntax.NodeSyntax[Rdf](node)

  implicit def uriSyntax[Rdf <: RDF](uri: Rdf#URI)(implicit ops: RDFOps[Rdf]): syntax.URISyntax[Rdf] = new syntax.URISyntax[Rdf](uri)

  implicit def literalSyntax[Rdf <: RDF](literal: Rdf#Literal)(implicit ops: RDFOps[Rdf]): syntax.LiteralSyntax[Rdf] = new syntax.LiteralSyntax[Rdf](literal)

  implicit def typedLiteralSyntax[Rdf <: RDF](tl: Rdf#TypedLiteral)(implicit ops: RDFOps[Rdf]): syntax.TypedLiteralSyntax[Rdf] = new syntax.TypedLiteralSyntax[Rdf](tl)

  implicit def langLiteralSyntax[Rdf <: RDF](ll: Rdf#LangLiteral)(implicit ops: RDFOps[Rdf]): syntax.LangLiteralSyntax[Rdf] = new syntax.LangLiteralSyntax[Rdf](ll)

  implicit def stringSyntax(s: String): syntax.StringSyntax = new syntax.StringSyntax(s)

  implicit def anySyntax[T](t: T): syntax.AnySyntax[T] = new syntax.AnySyntax[T](t)

}

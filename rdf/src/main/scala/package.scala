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

  implicit def graphSyntax[Rdf <: RDF](graph: Rdf#Graph): syntax.GraphSyntax[Rdf] = new syntax.GraphSyntax[Rdf](graph)

  implicit def nodeSyntax[Rdf <: RDF](node: Rdf#Node): syntax.NodeSyntax[Rdf] = new syntax.NodeSyntax[Rdf](node)

  implicit def uriSyntax[Rdf <: RDF](uri: Rdf#URI): syntax.URISyntax[Rdf] = new syntax.URISyntax[Rdf](uri)

  implicit def literalSyntax[Rdf <: RDF](literal: Rdf#Literal): syntax.LiteralSyntax[Rdf] = new syntax.LiteralSyntax[Rdf](literal)

  implicit def typedLiteralSyntax[Rdf <: RDF](tl: Rdf#TypedLiteral): syntax.TypedLiteralSyntax[Rdf] = new syntax.TypedLiteralSyntax[Rdf](tl)

  implicit def langLiteralSyntax[Rdf <: RDF](ll: Rdf#LangLiteral): syntax.LangLiteralSyntax[Rdf] = new syntax.LangLiteralSyntax[Rdf](ll)

  implicit def stringSyntax(s: String): syntax.StringSyntax = new syntax.StringSyntax(s)

  implicit def anySyntax[T](t: T): syntax.AnySyntax[T] = new syntax.AnySyntax[T](t)

  implicit def sparqlSolutionSyntax[Rdf <: RDF](solution: Rdf#Solution): syntax.SparqlSolutionSyntax[Rdf] = new syntax.SparqlSolutionSyntax[Rdf](solution)

  implicit def sparqlSolutionsSyntax[Rdf <: RDF](solutions: Rdf#Solutions): syntax.SparqlSolutionsSyntax[Rdf] = new syntax.SparqlSolutionsSyntax[Rdf](solutions)

  implicit def toPointedGraphW[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraphW[Rdf] = new PointedGraphW[Rdf](PointedGraph(node)(ops))

}

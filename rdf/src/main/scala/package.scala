package org.w3

import scala.util._
import scala.concurrent._
import scala.concurrent.duration._

package object banana
extends BananaRDFWriterSelector
with BananaSparqlSolutionWriterSelector {

  type TripleMatch[Rdf <: RDF] = (Rdf#NodeMatch, Rdf#NodeMatch, Rdf#NodeMatch)

  type SparqlSolutionsWriter[Rdf <: RDF, +T] = Writer[Rdf#Solutions, T]

  type RDFWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Graph]

  type SparqlSolutionsWriterSelector[Rdf <: RDF] = WriterSelector[Rdf#Solutions]

  implicit class FutureW[T](f: Future[T]) {
    def getOrFail(duration: Duration = Duration("3s")): T = {
      Await.result(f, duration)
    }
  }

  implicit class TryW[T](t: Try[T]) {
    def asFuture: Future[T] = t match {
      case Success(s) => Future.successful(s)
      case Failure(f) => Future.failed(f)
    }
  }

}

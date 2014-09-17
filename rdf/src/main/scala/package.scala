package org.w3

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

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

  /**
   * Same thread execution context. Turns a Future[C] into a C effectively
   * See discussion on scala-user "Calling Thread Execution Context"
   * https://groups.google.com/forum/#!topic/scala-user/FO3gJmxe9kA
   * @return the Execution Context
   */
  def sameThreadExecutionContext = new ExecutionContext {
    def reportFailure(t: Throwable) { t.printStackTrace() }
    def execute(runnable: Runnable) { runnable.run() }
  }

  implicit class TryW[T](t: Try[T]) {
    def asFuture: Future[T] = t match {
      case Success(s) => Future.successful(s)
      case Failure(f) => Future.failed(f)
    }
  }

}

package org.w3.banana

import org.w3.banana.util._
import scalaz._
import scalaz.Scalaz._

object LinkedDataStore {

  implicit def apply[Rdf <: RDF](store: AsyncGraphStore[Rdf])(implicit diesel: Diesel[Rdf]): LinkedDataStore[Rdf] =
    new LinkedDataStore[Rdf](store)(diesel)

}

class LinkedDataStore[Rdf <: RDF](store: AsyncGraphStore[Rdf])(implicit diesel: Diesel[Rdf]) {

  import diesel._
  import ops._

  /**
   * returns a LinkedDataResource
   * - the fragment-less uri is the support document uri
   * - the document content represents the graph itself
   * - the given is the pointer in the graph
   */
  def get(hyperlink: Rdf#URI): BananaFuture[LinkedDataResource[Rdf]] = {
    val docUri = hyperlink.fragmentLess
    store.getGraph(docUri) map { graph =>
      val pointed = PointedGraph(hyperlink, graph).resolveAgainst(docUri)
      LinkedDataResource(docUri, pointed)
    }
  }

  def get(hyperlinks: Iterable[Rdf#URI]): BananaFuture[Set[LinkedDataResource[Rdf]]] =
    FutureValidation.sequence(hyperlinks.map(get)).map(_.toSet)

  /**
   * saves the pointed graph using the (fragment-less) pointer as the document uri
   *
   * - the graph at the underlying document is not overriden, we only append triples
   * - if the graph did not previously exist, it is created
   */
  def append(uri: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[Unit] = {
    val docUri = uri.fragmentLess
    store.appendToGraph(docUri, pointed.graph.resolveAgainst(docUri))
  }

  def patch(uri: Rdf#URI, delete: PointedGraph[Rdf]): BananaFuture[Unit] = {
    val docUri = uri.fragmentLess
    store.patchGraph(docUri, delete.graph.resolveAgainst(docUri), emptyGraph)
  }

  /**
   * 
   */
  def post(collection: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[Rdf#URI] = {
    for {
      fragment <- {
        pointed.pointer.as[Rdf#URI] flatMap { uri =>
          if (uri.isPureFragment) Success(uri) else Failure(NotPureFragment)
        }
      }.bf
      docUri = collection.newChildUri
      _ <- append(docUri, pointed)
    } yield {
      docUri.resolve(fragment.toString)
    }
  }

  def delete(uri: Rdf#URI): BananaFuture[Unit] = store.removeGraph(uri)

  def put(ldr: LinkedDataResource[Rdf]): BananaFuture[Unit] =
    this.delete(ldr.uri) flatMap { _ =>
      this.append(ldr.uri, ldr.resource) map { _ => () }
    }


}

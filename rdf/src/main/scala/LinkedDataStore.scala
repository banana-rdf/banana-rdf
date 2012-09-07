package org.w3.banana

import org.w3.banana.util._
import scalaz._
import scalaz.Scalaz._
import akka.dispatch.ExecutionContext

object LinkedDataStore {

  implicit def apply[Rdf <: RDF](store: RDFStore[Rdf, BananaFuture])(implicit diesel: Diesel[Rdf], ec: ExecutionContext): LinkedDataStore[Rdf] =
    new LinkedDataStore[Rdf](store)(diesel, ec)

}

class LinkedDataStore[Rdf <: RDF](store: RDFStore[Rdf, BananaFuture])(implicit diesel: Diesel[Rdf], ec: ExecutionContext) {

  import diesel._
  import ops._

  /**
   * returns a LinkedDataResource
   * - the fragment-less uri is the support document uri
   * - the document content represents the graph itself
   * - the given is the pointer in the graph
   */
  def GET(hyperlink: Rdf#URI): BananaFuture[LinkedDataResource[Rdf]] = {
    store.execute(Command.GET[Rdf](hyperlink))
  }

  def GET(hyperlinks: Iterable[Rdf#URI]): BananaFuture[Set[LinkedDataResource[Rdf]]] = {
    store.execute(Command.GET[Rdf](hyperlinks))
  }

  /**
   * saves the pointed graph using the (fragment-less) pointer as the document uri
   *
   * - the graph at the underlying document is not overriden, we only append triples
   * - if the graph did not previously exist, it is created
   */
  def POST(uri: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[Unit] = {
    store.execute(Command.POST[Rdf](uri, pointed))
  }

  def PATCH(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]]): BananaFuture[Unit] = {
    store.execute(Command.PATCH[Rdf](uri, delete))
  }

  /**
   *
   */
  def POSTToCollection(collection: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[Rdf#URI] = {
    store.execute(Command.POSTToCollection[Rdf](collection, pointed))
  }

  def DELETE(uri: Rdf#URI): BananaFuture[Unit] = {
    store.execute(Command.DELETE[Rdf](uri))
  }

  def PUT(ldr: LinkedDataResource[Rdf]): BananaFuture[Unit] = {
    store.execute(Command.PUT[Rdf](ldr))
  }

}

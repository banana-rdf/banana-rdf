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

  /**
   * saves the pointed graph using the (fragment-less) pointer as the document uri
   *
   * - the graph at the underlying document is not overriden, we only append triples
   * - if the graph did not previously exist, it is created
   */
  def append(docUri: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[Unit] = {
    store.appendToGraph(docUri, pointed.graph.relativize(docUri))
  }

  /**
   * 
   */
  def post(collection: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[Rdf#URI] = {
    val docUri = collection.newChildUri
    append(docUri, pointed.relativize(docUri)) map { _ => docUri }
  }

}

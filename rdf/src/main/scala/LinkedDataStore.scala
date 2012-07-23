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

  /**
   * returns a LinkedDataResource
   * - the fragment-less uri is the support document uri
   * - the document content represents the graph itself
   * - the given is the pointer in the graph
   */
  def get(uri: Rdf#URI): BananaFuture[LinkedDataResource[Rdf]] = {
    val noFragUri = uri.fragmentLess
    store.getGraph(noFragUri) map { graph =>
      val pointed = PointedGraph(uri, graph)
      LinkedDataResource(noFragUri, pointed)
    }
  }

  /**
   * saves the pointed graph using the (fragment-less) pointer as the document uri
   *
   * - the graph at the underlying document is not overriden, we only append triples
   * - if the graph did not previously exist, it is created
   */
  def append(pointed: PointedGraph[Rdf]): BananaFuture[LinkedDataResource[Rdf]] = {
    pointed.as[Rdf#URI].bf flatMap { uri =>
      store.appendToGraph(uri, pointed.graph) map { _ =>
        LinkedDataResource(uri, pointed)
      }
    }
  }



//  def getAll[T](in: Rdf#URI, classUri: Rdf#URI)(implicit binder: PointedGraphBinder[Rdf, T]): BananaFuture[Iterable[T]] = {
//    store.getNamedGraph(in) flatMap { graph =>
//      val ts: Iterable[BananaValidation[T]] = graph.getAllInstancesOf(classUri) map { _.as[T] }
//      val result: BananaValidation[Iterable[T]] = ts.toList.sequence[BananaValidation, T]
//      result.bf
//    }
//  }
//
//  // classUris.classes should respect the invariant that there is always at least one uri, and this uri is assumed to be the most precise type
//  def getAll[T](in: Rdf#URI)(implicit classUris: ClassUrisFor[Rdf, T], binder: PointedGraphBinder[Rdf, T]): BananaFuture[Iterable[T]] = {
//    val classUri = classUris.classes.head
//    getAll[T](in, classUri)
//  }
//

}

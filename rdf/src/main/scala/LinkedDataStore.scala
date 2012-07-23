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

  def put(atUri: Rdf#URI, pointed: PointedGraph[Rdf]): BananaFuture[LinkedDataResource[Rdf]] = {
    store.appendToNamedGraph(atUri, pointed.graph) map { _ =>
      LinkedDataResource(atUri, pointed)
    }
  }

  def put(pointed: PointedGraph[Rdf]): BananaFuture[LinkedDataResource[Rdf]] = {
    pointed.as[Rdf#URI].bf flatMap { uri =>
      store.appendToNamedGraph(uri, pointed.graph) map { _ =>
        LinkedDataResource(uri, pointed)
      }
    }
  }

  def get(uri: Rdf#URI): BananaFuture[LinkedDataResource[Rdf]] = {
    val noFragUri = uri.fragmentLess
    store.getNamedGraph(noFragUri) map { graph =>
      val pointed = PointedGraph(uri, graph)
      LinkedDataResource(noFragUri, pointed)
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

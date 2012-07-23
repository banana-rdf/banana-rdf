package org.w3.banana

import org.w3.banana.util._
import scalaz._
import scalaz.Scalaz._

object ObjectStore {

  implicit def apply[Rdf <: RDF](store: AsyncGraphStore[Rdf])(implicit diesel: Diesel[Rdf]): ObjectStore[Rdf] =
    new ObjectStore[Rdf](store)(diesel)

}

class ObjectStore[Rdf <: RDF](store: AsyncGraphStore[Rdf])(implicit diesel: Diesel[Rdf]) {

  import diesel._

  /**
   * saves an object if we know how to make a graph from it, and how to give it a URI
   */
  def save[T](pointed: PointedGraph[Rdf], atGraph: Rdf#URI): BananaFuture[Unit] = {
    store.appendToNamedGraph(atGraph, pointed.graph)
  }

  def save[T](pointed: PointedGraph[Rdf]): BananaFuture[Unit] = {
    pointed.as[Rdf#URI].bf flatMap { uri =>
      store.appendToNamedGraph(uri, pointed.graph)
    }
  }

  def get[T](uri: Rdf#URI)(implicit binder: PointedGraphBinder[Rdf, T]): BananaFuture[T] = {
    store.getNamedGraph(uri) flatMap { graph =>
      val pointed = PointedGraph(uri, graph)
      pointed.as[T]
    }
  }

  def getAll[T](in: Rdf#URI, classUri: Rdf#URI)(implicit binder: PointedGraphBinder[Rdf, T]): BananaFuture[Iterable[T]] = {
    store.getNamedGraph(in) flatMap { graph =>
      val ts: Iterable[BananaValidation[T]] = graph.getAllInstancesOf(classUri) map { _.as[T] }
      val result: BananaValidation[Iterable[T]] = ts.toList.sequence[BananaValidation, T]
      result.bf
    }
  }

  // classUris.classes should respect the invariant that there is always at least one uri, and this uri is assumed to be the most precise type
  def getAll[T](in: Rdf#URI)(implicit classUris: ClassUrisFor[Rdf, T], binder: PointedGraphBinder[Rdf, T]): BananaFuture[Iterable[T]] = {
    val classUri = classUris.classes.head
    getAll[T](in, classUri)
  }

}

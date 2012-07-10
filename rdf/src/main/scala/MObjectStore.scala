package org.w3.banana

import org.w3.banana.util._

class ObjectStore[Rdf <: RDF](store: AsyncGraphStore[Rdf])(implicit diesel: Diesel[Rdf]) {

  def save[T, TG](t: T)(implicit binder: PointedGraphBinder[Rdf, T], uriBinder: URIBinder[Rdf, T]): BananaFuture[Unit] = {
    for {
      pointed <- binder.toPointedGraph(t).bf
      uri <- uriBinder.toUri(t).bf
      r <- store.appendToNamedGraph(uri, pointed.graph)
    } yield {
      r
    }
  }

}

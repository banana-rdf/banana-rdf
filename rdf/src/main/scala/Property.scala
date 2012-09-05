package org.w3.banana

import org.w3.banana.util._

trait Property[Rdf <: RDF, T] {

  val uri: Rdf#URI

  def pos(t: T): Iterable[(Rdf#URI, PointedGraph[Rdf])]

  def extract(pointed: PointedGraph[Rdf]): BananaValidation[T]

}

object Property {

  implicit def propertyToUri[Rdf <: RDF](p: Property[Rdf, _]): Rdf#URI = p.uri

}

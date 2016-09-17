package org.w3.banana

import scala.util._

trait Property[Rdf <: RDF, T] {

  def uri: Rdf#URI

  def pos(t: T): Iterable[(Rdf#URI, PointedGraph[Rdf])]

  def extract(pointed: PointedGraph[Rdf]): Try[T]

}

object Property {

  implicit def propertyToUri[Rdf <: RDF](p: Property[Rdf, _]): Rdf#URI = p.uri

}

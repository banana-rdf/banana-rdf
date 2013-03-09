package org.w3.banana

import scala.util._

trait Property[Rdf <: RDF, T] {

  def pos(t: T): Iterable[(Rdf#URI, PointedGraph[Rdf])]

  def extract(pointed: PointedGraph[Rdf]): Try[T]

}
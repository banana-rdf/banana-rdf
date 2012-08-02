package org.w3.banana

import org.w3.banana.util.BananaValidation

trait Property[Rdf <: RDF, T] {

  val uri: Rdf#URI

  val binder: PointedGraphBinder[Rdf, T]

  def extract(pointed: PointedGraph[Rdf]): BananaValidation[T]

}

object Property {

  implicit def propertyToUri[Rdf <: RDF](p: Property[Rdf, _]): Rdf#URI = p.uri

}

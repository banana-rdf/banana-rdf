package org.w3.banana

sealed trait Field[Rdf <: RDF, T]

case class UriComponent[Rdf <: RDF, T](binder: URIBinder[Rdf, T]) extends Field[Rdf, T]

case class Property[Rdf <: RDF, T](uri: Rdf#URI, binder: PointedGraphBinder[Rdf, T]) extends Field[Rdf, T]

object Property {

  implicit def propertyToUri[Rdf <: RDF](p: Property[Rdf, _]): Rdf#URI = p.uri

}

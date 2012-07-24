package org.w3.banana

case class Property[Rdf <: RDF, T](uri: Rdf#URI, binder: PointedGraphBinder[Rdf, T])

object Property {

  implicit def propertyToUri[Rdf <: RDF](p: Property[Rdf, _]): Rdf#URI = p.uri

}

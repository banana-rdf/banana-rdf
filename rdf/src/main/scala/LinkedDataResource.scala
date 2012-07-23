package org.w3.banana

case class LinkedDataResource[Rdf <: RDF](uri: Rdf#URI, resource: PointedGraph[Rdf])

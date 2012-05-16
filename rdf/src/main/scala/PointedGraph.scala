package org.w3.banana

case class PointedGraph[Rdf <: RDF](node: Rdf#Node, graph: Rdf#Graph)

case class PointedGraphs[Rdf <: RDF](nodes: Iterable[Rdf#Node], graph: Rdf#Graph)

package org.w3.banana

trait RDFGraphTraversal[Rdf <: RDF] {

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#URI): Iterable[Rdf#Node]

  def getPredicates(graph: Rdf#Graph, subject: Rdf#Node): Iterable[Rdf#URI]

  def getSubjects(graph: Rdf#Graph, predicate: Rdf#URI, obj: Rdf#Node): Iterable[Rdf#Node]

}

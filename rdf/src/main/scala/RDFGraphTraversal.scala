package org.w3.banana

import scalaz._

trait RDFGraphTraversal[Rdf <: RDF] {

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#IRI): Iterable[Rdf#Node]

  def getPredicates(graph: Rdf#Graph, subject: Rdf#Node): Iterable[Rdf#IRI]
}

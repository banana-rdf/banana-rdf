package org.w3.rdf

import scalaz._

trait RDFGraphTraversal[Rdf <: RDF] {

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#IRI): Iterable[Rdf#Node]

}

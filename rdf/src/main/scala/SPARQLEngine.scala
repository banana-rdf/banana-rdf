package org.w3.banana

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF] {

  def executeSelect(query: Rdf#SelectQuery): Rdf#Solutions

  def executeConstruct(query: Rdf#ConstructQuery): Rdf#Graph

  def executeAsk(query: Rdf#AskQuery): Boolean

}

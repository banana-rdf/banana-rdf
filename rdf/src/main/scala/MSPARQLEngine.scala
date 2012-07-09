package org.w3.banana

/**
 * to execute SPARQL queries
 */
trait MSPARQLEngine[Rdf <: RDF, M[_]] {

  def executeSelect(query: Rdf#SelectQuery): M[Rdf#Solutions]

  def executeConstruct(query: Rdf#ConstructQuery): M[Rdf#Graph]

  def executeAsk(query: Rdf#AskQuery): M[Boolean]

}

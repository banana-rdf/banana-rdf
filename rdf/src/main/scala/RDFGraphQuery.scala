package org.w3.banana

/**
 * to execute SPARQL queries on an RDF graph
 */
trait RDFGraphQuery[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelect(graph: Rdf#Graph, query: Sparql#SelectQuery): Iterable[PartialFunction[String, Rdf#Node]]

  def executeConstruct(graph: Rdf#Graph, query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(graph: Rdf#Graph, query: Sparql#AskQuery): Boolean

}

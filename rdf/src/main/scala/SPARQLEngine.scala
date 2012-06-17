package org.w3.banana

import scalaz.{Left3, Right3, Middle3, Either3}

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF, Sparql <: SPARQL]  {

  def executeSelect(query: Sparql#SelectQuery): Iterable[PartialFunction[String, Rdf#Node]]

  def executeConstruct(query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(query: Sparql#AskQuery): Boolean

}

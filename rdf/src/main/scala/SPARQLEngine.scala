package org.w3.banana

import scalaz.{Left3, Right3, Middle3, Either3}

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF, Sparql <: SPARQL]  {

  /**
   * This returns the underlying objects, which is useful when needing to serialise the answer
   * for example
   * @param query
   * @return
   */
  def executeSelectPlain(query: Sparql#SelectQuery): Sparql#Answers

  def executeSelect(query: Sparql#SelectQuery): Iterable[Row[Rdf]]

  def executeConstruct(query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(query: Sparql#AskQuery): Boolean

}

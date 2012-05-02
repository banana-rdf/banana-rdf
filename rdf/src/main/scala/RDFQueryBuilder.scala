package org.w3.rdf

trait SPARQLQueryBuilder[Sparql <: SPARQL] {

  def SelectQuery(query: String): Sparql#SelectQuery

  def ConstructQuery(query: String): Sparql#ConstructQuery

  def AskQuery(query: String): Sparql#AskQuery

}

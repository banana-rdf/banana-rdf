package org.w3.rdf

trait SPARQLQueryBuilder[Rdf <: RDF, Sparql <: SPARQL] {

  def SelectQuery(query: String): Sparql#SelectQuery

  def ConstructQuery(query: String): Sparql#ConstructQuery

  def AskQuery(query: String): Sparql#AskQuery

  def SelectQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Sparql#SelectQuery

  // def ConstructQuery(query: String): Sparql#ConstructQuery

  // def AskQuery(query: String): Sparql#AskQuery

}

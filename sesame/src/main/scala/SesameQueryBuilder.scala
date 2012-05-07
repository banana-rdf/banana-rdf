package org.w3.rdf.sesame

import org.w3.rdf._

object SesameQueryBuilder extends SPARQLQueryBuilder[Sesame, SesameSPARQL] {

  def SelectQuery(query: String): SesameSPARQL#SelectQuery = query
    
  def ConstructQuery(query: String): SesameSPARQL#ConstructQuery = query

  def AskQuery(query: String): SesameSPARQL#AskQuery = query

}

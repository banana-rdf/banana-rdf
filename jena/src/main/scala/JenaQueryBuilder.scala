package org.w3.rdf.jena

import org.w3.rdf._

import com.hp.hpl.jena.query._

object JenaQueryBuilder extends SPARQLQueryBuilder[Jena, JenaSPARQL] {

  def SelectQuery(query: String): JenaSPARQL#SelectQuery = QueryFactory.create(query)
    
  def ConstructQuery(query: String): JenaSPARQL#ConstructQuery = QueryFactory.create(query)

  def AskQuery(query: String): JenaSPARQL#AskQuery = QueryFactory.create(query)

}

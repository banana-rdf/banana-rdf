package org.w3.rdf.jena

import org.w3.rdf._

import com.hp.hpl.jena.query._

object JenaQueryBuilder extends SPARQLQueryBuilder[Jena, JenaSPARQL] {

  def SelectQuery(query: String): JenaSPARQL#SelectQuery = QueryFactory.create(query)
    
  def ConstructQuery(query: String): JenaSPARQL#ConstructQuery = QueryFactory.create(query)

  def AskQuery(query: String): JenaSPARQL#AskQuery = QueryFactory.create(query)

  /**************/

  def buildQuery(query: String, prefixes: Seq[Prefix[Jena]]): String = {
    val builder = new java.lang.StringBuilder
    prefixes foreach { prefix =>
      val prefixDefinition = "prefix %s: <%s>\n" format (prefix.prefixName, prefix.prefixIri)
      builder.append(prefixDefinition)
    }
    builder.append(query)
    builder.toString
  }

  def SelectQuery(query: String, prefix: Prefix[Jena], prefixes: Prefix[Jena]*): JenaSPARQL#SelectQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    SelectQuery(completeQuery)
  }


}

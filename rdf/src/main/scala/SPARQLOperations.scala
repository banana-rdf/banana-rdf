package org.w3.banana

trait SPARQLOperations[Rdf <: RDF, Sparql <: SPARQL] {

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def SelectQuery(query: String): Sparql#SelectQuery

  def ConstructQuery(query: String): Sparql#ConstructQuery

  def AskQuery(query: String): Sparql#AskQuery

  def Query(query: String): Sparql#Query

  /**************/

  private def buildQuery(query: String, prefixes: Seq[Prefix[Rdf]]): String = {
    val builder = new java.lang.StringBuilder
    prefixes foreach { prefix =>
      val prefixDefinition = "prefix %s: <%s>\n" format (prefix.prefixName, prefix.prefixIri)
      builder.append(prefixDefinition)
    }
    builder.append(query)
    builder.toString
  }

  def SelectQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Sparql#SelectQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    SelectQuery(completeQuery)
  }

  def ConstructQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Sparql#ConstructQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    ConstructQuery(completeQuery)
  }

  def AskQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Sparql#AskQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    AskQuery(completeQuery)
  }

}

package org.w3.banana

import scalaz.Validation

trait SPARQLOperations[Rdf <: RDF, Sparql <: SPARQL] {

  def SelectQuery(query: String): Sparql#SelectQuery

  def ConstructQuery(query: String): Sparql#ConstructQuery

  def AskQuery(query: String): Sparql#AskQuery

  /**
   * A general query constructor. When this is used it is usually
   * because the query type is not known in advance, ( as when a query is received
   * over the internet). As a result the response is a validation, as the query
   * may not have been tested for validity.
   * @param query a SPARQL query
   * @return A validation containing the Query
   */
  def Query(query: String): Validation[Exception,Sparql#Query]

  /**
   * A fold operation.
   * The types returned will be very disjunctive. Consider having T be a scalaz.Either3
   */
  def fold[T](query: Sparql#Query)(select: Sparql#SelectQuery => T,
                                   construct: Sparql#ConstructQuery => T,
                                   ask: Sparql#AskQuery => T): T

  def getNode(solution: Sparql#Solution, v: String): Validation[BananaException, Rdf#Node]

  def varnames(solution: Sparql#Solution): Set[String]

  def solutionIterator(solutions: Sparql#Solutions): Iterable[Sparql#Solution]

  /**************/

  // TODO move all that stuff: there should be only function definitions here

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

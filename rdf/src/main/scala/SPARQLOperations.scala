package org.w3.banana

import scalaz.Validation

trait SPARQLOperations[Rdf <: RDF] {

  def SelectQuery(query: String): Rdf#SelectQuery

  def ConstructQuery(query: String): Rdf#ConstructQuery

  def AskQuery(query: String): Rdf#AskQuery

  /**
   * A general query constructor. When this is used it is usually
   * because the query type is not known in advance, ( as when a query is received
   * over the internet). As a result the response is a validation, as the query
   * may not have been tested for validity.
   * @param query a SPARQL query
   * @return A validation containing the Query
   */
  def Query(query: String): Validation[Exception, Rdf#Query]

  /**
   * A fold operation.
   * The types returned will be very disjunctive. Consider having T be a scalaz.Either3
   */
  def fold[T](query: Rdf#Query)(select: Rdf#SelectQuery => T,
    construct: Rdf#ConstructQuery => T,
    ask: Rdf#AskQuery => T): T

  def getNode(solution: Rdf#Solution, v: String): Validation[BananaException, Rdf#Node]

  def varnames(solution: Rdf#Solution): Set[String]

  def solutionIterator(solutions: Rdf#Solutions): Iterable[Rdf#Solution]

  /* provides syntax for the Solution and Solutions */

  implicit def solutionSyntax(solution: Rdf#Solution): SPARQLSolutionSyntax[Rdf] = SPARQLSolutionSyntax(solution)(this)

  implicit def solutionsSyntax(solutions: Rdf#Solutions): SPARQLSolutionsSyntax[Rdf] = SPARQLSolutionsSyntax(solutions)(this)

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

  def SelectQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Rdf#SelectQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    SelectQuery(completeQuery)
  }

  def ConstructQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Rdf#ConstructQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    ConstructQuery(completeQuery)
  }

  def AskQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Rdf#AskQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    AskQuery(completeQuery)
  }

}

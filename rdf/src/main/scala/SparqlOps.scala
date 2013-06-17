package org.w3.banana

import scala.util._

object SparqlOps {

  def apply[Rdf <: RDF](implicit sparqlOps: SparqlOps[Rdf]): SparqlOps[Rdf] = sparqlOps

}

trait SparqlOps[Rdf <: RDF] {

  def SelectQuery(query: String): Rdf#SelectQuery

  def ConstructQuery(query: String): Rdf#ConstructQuery

  def AskQuery(query: String): Rdf#AskQuery

  def UpdateQuery(query: String): Rdf#UpdateQuery

  /**
   * A general query constructor. When this is used it is usually
   * because the query type is not known in advance, ( as when a query is received
   * over the internet). As a result the response is a validation, as the query
   * may not have been tested for validity.
   * @param query a Sparql query
   * @return A validation containing the Query
   */
  def Query(query: String): Try[Rdf#Query]

  /**
   * A fold operation.
   * The types returned will be very disjunctive. Consider having T be a scalaz.Either3
   */
  def fold[T](query: Rdf#Query)(
    select: Rdf#SelectQuery => T,
    construct: Rdf#ConstructQuery => T,
    ask: Rdf#AskQuery => T): T

  def getNode(solution: Rdf#Solution, v: String): Try[Rdf#Node]

  def varnames(solution: Rdf#Solution): Set[String]

  def solutionIterator(solutions: Rdf#Solutions): Iterable[Rdf#Solution]

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

  def UpdateQuery(query: String, prefix: Prefix[Rdf], prefixes: Prefix[Rdf]*): Rdf#UpdateQuery = {
    val completeQuery = buildQuery(query, prefix +: prefixes.toSeq)
    UpdateQuery(completeQuery)
  }

}

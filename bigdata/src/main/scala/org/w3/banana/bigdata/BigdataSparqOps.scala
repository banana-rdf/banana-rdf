package org.w3.banana.bigdata

import com.bigdata.rdf.model.BigdataValue
import com.bigdata.rdf.sail.BigdataSailUpdate
import org.openrdf.query.BindingSet
import org.w3.banana.{VarNotFound, Prefix, SparqlOps}
import scala.collection.JavaConversions._
import scala.util.{Success, Failure, Try}

class BigdataSparqOps extends SparqlOps[Bigdata]{
  override def parseSelect(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[String] = Try(query)

  override def solutionIterator(solutions: Vector[BindingSet]): Iterator[BindingSet] = solutions.iterator

  override def parseConstruct(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[String] = Try(query)

  override def varnames(solution: BindingSet): Set[String] = solution.getBindingNames.toSet

  /**
   * A general query constructor.
   *
   * When this is used it is usually because the query type is not
   * known in advance, ( as when a query is received over the
   * internet). As a result the response is a validation, as the
   * query may not have been tested for validity.
   *
   * @param query a Sparql query
   * @return A validation containing the Query
   */
  override def parseQuery(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[String] = Try(query)

  /**
   * A fold operation.
   * The types returned will be very disjunctive. Consider having T be a scalaz.Either3
   */
  override def fold[T](
    query: String)(
    select: (String) => T,
    construct: (String) => T,
    ask: (String) => T): T = {
    select(query) //NOTE: until the dependency from connection is not fix we use just strings
  }

  override def parseAsk(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[String] = Try(query)

  override def getNode(solution: BindingSet, v: String): Try[BigdataValue] =  solution.getValue(v) match {
      case null=>
        Failure(VarNotFound("var " + v + " not found in BindingSet " + solution.toString))
      case node:BigdataValue=>
        Success(node)
      case other=>
        Failure(new IllegalArgumentException("value s not bigdata value"))
    }


  override def parseUpdate(query: String, prefixes: Seq[Prefix[Bigdata]]): Try[Bigdata#UpdateQuery] = Try(query)
}

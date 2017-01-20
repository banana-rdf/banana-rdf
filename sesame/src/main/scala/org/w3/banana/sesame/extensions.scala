package org.w3.banana
package sesame

import org.openrdf.model.Statement
import org.openrdf.query.{GraphQueryResult, BindingSet, TupleQueryResult}
import org.openrdf.repository.RepositoryResult

/** small things to make life with Sesame easier */
object extensions extends SesameResultsConversions


trait SesameResultsConversions {

  /**
   * Implicit class that turns query result into iterator (so methods toList, map and so on can be applied to it)
   *
   * @param results results in sesame format
   */
  implicit class TupleResult(results: TupleQueryResult) extends Iterator[BindingSet]
  {

    override def next(): BindingSet = results.next()

    override def hasNext: Boolean = results.hasNext
  }

  implicit class StatementsResult(results:RepositoryResult[Statement]) extends Iterator[Statement]{

    override def next(): Statement = results.next()

    override def hasNext: Boolean = results.hasNext

  }

  implicit class GraphResult(results:GraphQueryResult) extends Iterator[Statement]
  {
    override def next(): Statement = results.next()

    override def hasNext: Boolean = results.hasNext
  }

}
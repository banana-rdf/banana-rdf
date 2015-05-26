package org.w3.banana

import org.openrdf.model.{URI, Resource, Statement, Value}
import org.openrdf.query.{GraphQueryResult, BindingSet, TupleQueryResult}
import org.openrdf.repository.RepositoryResult
import scala.collection.immutable.{Map, List}

/**
 * I put here some implicits to add Iterable support for bigdata StatementIterators
 * as they do not implement Java iterator/iterable so JavaConversions do not work for them
 */
package object bigdata extends BigdataResultsConversions

trait BigdataResultsConversions
{

  /**
   * Implicit class that turns  query result into iterator (so methods toList, map and so on can be applied to it)
   * @param results results in sesame format
   */
  implicit class TupleResult(results: TupleQueryResult)  extends Iterator[BindingSet]
  {

    override def next(): BindingSet = results.next()

    override def hasNext: Boolean = results.hasNext
  }

  /*
implicit class for Repository results that adds some nice features there and turnes it into Scala Iterator
*/
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

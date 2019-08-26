package org.w3.banana.rd4j.helper

import org.eclipse.rdf4j.query.QueryResult

import scala.collection.{AbstractIterator, Iterator}
import scala.collection.immutable.Stream

final class QueryResultStreamIterator[+A](queryResults: QueryResult[A]) extends AbstractIterator[A] with Iterator[A] {
  override def hasNext: Boolean = queryResults.hasNext

  override def next(): A = queryResults.next()
}

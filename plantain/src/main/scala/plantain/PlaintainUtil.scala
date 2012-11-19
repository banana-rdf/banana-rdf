package org.w3.banana.plantain

import org.openrdf.query.{ BindingSet, TupleQueryResult }
import org.openrdf.query.impl.MapBindingSet
import info.aduna.iteration.CloseableIteration
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl.ContextStatementImpl

object PlantainUtil {

  private class CloseableIterationAsIterator[+T](iteration: CloseableIteration[T, _]) extends Iterator[T] {
    def hasNext: Boolean = iteration.hasNext
    def next(): T = iteration.next()
  }

  implicit class CloseableIterationW[T](val iteration: CloseableIteration[T, _]) extends AnyVal {
    def toIterator: Iterator[T] = new CloseableIterationAsIterator[T](iteration)
  }

  private class IteratorAsCloseableIteration[T, E <: Exception](iterator: Iterator[T]) extends CloseableIteration[T, E] {
   def close(): Unit = ()
   def hasNext(): Boolean = iterator.hasNext
   def next(): T = iterator.next
   def remove(): Unit = throw new UnsupportedOperationException
  }

  implicit class IteratorW[T](val iterator: Iterator[T]) extends AnyVal {
    def toCloseableIteration[E <: Exception]: CloseableIteration[T, E] =
      new IteratorAsCloseableIteration[T, E](iterator)
  }

  implicit class Bindings(val bindings: Map[String, Plantain#Node]) extends AnyVal {
    def asSesame: BindingSet = {
      val bindingSet = new MapBindingSet(bindings.size)
      bindings foreach { case (name, value) => bindingSet.addBinding(name, value.asSesame) }
      bindingSet
    }
  }

  implicit class StatementW(val statement: Statement) extends AnyVal {
    def withContext(context: Resource): Statement =
      new ContextStatementImpl(
        statement.getSubject,
        statement.getPredicate,
        statement.getObject,
        context)
  }

}

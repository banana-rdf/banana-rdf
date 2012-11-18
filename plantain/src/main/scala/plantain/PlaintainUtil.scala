package org.w3.banana.plantain

import org.openrdf.query.{ BindingSet, TupleQueryResult }
import org.openrdf.query.impl.MapBindingSet
import info.aduna.iteration.CloseableIteration

object PlantainUtil {

  private class CloseableIterationAsIterator(iteration: CloseableIteration[_ <: BindingSet, _]) extends Iterator[BindingSet] {
    def hasNext: Boolean = iteration.hasNext
    def next(): BindingSet = iteration.next()
  }

  implicit class CloseableIterationW(val iteration: CloseableIteration[_ <: BindingSet, _]) extends AnyVal {
    def toIterator: Iterator[BindingSet] = new CloseableIterationAsIterator(iteration)
  }

  implicit class Bindings(val bindings: Map[String, Plantain#Node]) extends AnyVal {
    def asSesame: BindingSet = {
      val bindingSet = new MapBindingSet(bindings.size)
      bindings foreach { case (name, value) => bindingSet.addBinding(name, value.asSesame) }
      bindingSet
    }
  }

}

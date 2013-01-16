package org.w3.banana.plantain

import org.openrdf.query.{ BindingSet, TupleQueryResult }
import org.openrdf.query.impl.MapBindingSet
import info.aduna.iteration.CloseableIteration
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl.ContextStatementImpl
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl

object PlantainUtil {

  def executeSelect(tripleSource: TripleSource, query: Plantain#SelectQuery, bindings: Map[String, Plantain#Node]): Plantain#Solutions = {
    val tupleExpr = query.getTupleExpr
    val evaluationStrategy = new EvaluationStrategyImpl(tripleSource)
    val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
    import collection.convert.wrapAsScala._
    BoundSolutions(results.toIterator,query.getTupleExpr.getBindingNames.toList)
  }

  def executeConstruct(tripleSource: TripleSource, query: Plantain#ConstructQuery, bindings: Map[String, Plantain#Node]): Plantain#Graph = {
    val tupleExpr = query.getTupleExpr
    val evaluationStrategy = new EvaluationStrategyImpl(tripleSource)
    val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
    val it = results.toIterator
    var resultGraph = Graph.empty
    it foreach { bindingSet =>
      try {
        val s = bindingSet.getValue("subject").asInstanceOf[Resource]
        val p = bindingSet.getValue("predicate").asInstanceOf[SesameURI]
        val o = bindingSet.getValue("object").asInstanceOf[Value]
        resultGraph += Triple(Node.fromSesame(s), Node.fromSesame(p), Node.fromSesame(o))
      } catch { case e: Exception => () }
    }
    resultGraph
  }

  def executeAsk(tripleSource: TripleSource, query: Plantain#AskQuery, bindings: Map[String, Plantain#Node]): Boolean = {
    val tupleExpr = query.getTupleExpr
    val evaluationStrategy = new EvaluationStrategyImpl(tripleSource)
    val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
    results.hasNext
  }

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

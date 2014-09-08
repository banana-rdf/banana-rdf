package org.w3.banana.plantain

import info.aduna.iteration.CloseableIteration
import org.openrdf.model.impl.ContextStatementImpl
import org.openrdf.model.{ URI => SesameURI, Graph => _, _ }
import org.openrdf.query.algebra._
import org.openrdf.query.algebra.evaluation.TripleSource
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl
import org.openrdf.query.algebra.helpers.StatementPatternCollector
import org.openrdf.query.impl.MapBindingSet
import org.openrdf.query.parser.sparql.SPARQLParserFactory
import org.openrdf.query.{ Binding, BindingSet }
import org.w3.banana.plantain.model._

import scala.collection.JavaConverters._
import scala.util.Try

object PlantainUtil {
  //todo: Is it a good idea to put this here?
  import model.Graph.vf
  private val p = new SPARQLParserFactory().getParser()

  def executeSelect(tripleSource: TripleSource, query: Plantain#SelectQuery, bindings: Map[String, Plantain#Node]): Plantain#Solutions = {
    val tupleExpr = query.getTupleExpr
    val evaluationStrategy = new EvaluationStrategyImpl(tripleSource, null, null)
    val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
    BoundSolutions(results.toIterator, query.getTupleExpr.getBindingNames.asScala.toList)
  }

  def executeConstruct(tripleSource: TripleSource, query: Plantain#ConstructQuery, bindings: Map[String, Plantain#Node]): Plantain#Graph = {
    val tupleExpr = query.getTupleExpr
    val evaluationStrategy = new EvaluationStrategyImpl(tripleSource, null, null)
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
    val evaluationStrategy = new EvaluationStrategyImpl(tripleSource, null, null)
    val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
    results.hasNext
  }

  private def deleteBoundTriples(graph: Plantain#Graph, whereBinding: BindingSet, deleteClause: TupleExpr): Plantain#Graph = {
    var resultGraph = graph
    if (deleteClause != null) {
      val deletePatterns = StatementPatternCollector.process(deleteClause);
      import scala.collection.convert.decorateAsScala._
      for (deletePattern <- deletePatterns.asScala) {

        val subject = getValueForVar(deletePattern.getSubjectVar(), whereBinding).asInstanceOf[Node]
        val predicate = getValueForVar(deletePattern.getPredicateVar(), whereBinding).asInstanceOf[URI]
        val obj = getValueForVar(deletePattern.getObjectVar(), whereBinding).asInstanceOf[Node];

        if (deletePattern.getContextVar() == null) {

          if (subject == null || predicate == null || obj == null) {
            // skip removal of triple if any variable is unbound (may happen
            // with optional patterns)
            // See SES-1047.

          } else {
            resultGraph = resultGraph.removeExistingTriple(Triple(subject, predicate, obj))
          }
        }
      }
    }
    resultGraph
  }

  private def insertBoundTriples(graph: Plantain#Graph, whereBinding: BindingSet, insertClause: TupleExpr): Plantain#Graph = {
    var resultGraph = graph
    if (insertClause != null) {
      val insertPatterns = StatementPatternCollector.process(insertClause);
      import scala.collection.convert.decorateAsScala._
      // bnodes in the insert pattern are locally scoped for each
      // individual source binding.
      val bnodeMapping = new MapBindingSet();
      for (insertPattern <- insertPatterns.asScala) {
        val toBeInserted = createStatementFromPattern(insertPattern, whereBinding, bnodeMapping);

        if (toBeInserted != null) {
          if (toBeInserted.getContext() == null) {
            resultGraph = graph + Triple.fromSesame(toBeInserted);
          }
        }
      }
    }
    resultGraph
  }

  private def createStatementFromPattern(pattern: StatementPattern, sourceBinding: BindingSet,
    bnodeMapping: MapBindingSet): Statement = {
    import org.openrdf.model._
    var subject: Resource = null
    var predicate: URI = null
    var `object`: Value = null
    var context: Resource = null
    if (pattern.getSubjectVar.hasValue) {
      subject = pattern.getSubjectVar.getValue.asInstanceOf[Resource]
    } else {
      subject = sourceBinding.getValue(pattern.getSubjectVar.getName).asInstanceOf[Resource]
      if (subject == null && pattern.getSubjectVar.isAnonymous) {
        val mappedSubject: Binding = bnodeMapping.getBinding(pattern.getSubjectVar.getName)
        if (mappedSubject != null) {
          subject = mappedSubject.getValue.asInstanceOf[Resource]
        } else {
          subject = vf.createBNode
          bnodeMapping.addBinding(pattern.getSubjectVar.getName, subject)
        }
      }
    }
    if (pattern.getPredicateVar.hasValue) {
      predicate = pattern.getPredicateVar.getValue.asInstanceOf[URI]
    } else {
      predicate = sourceBinding.getValue(pattern.getPredicateVar.getName).asInstanceOf[URI]
    }
    if (pattern.getObjectVar.hasValue) {
      `object` = pattern.getObjectVar.getValue
    } else {
      `object` = sourceBinding.getValue(pattern.getObjectVar.getName)
      if (`object` == null && pattern.getObjectVar.isAnonymous) {
        val mappedObject: Binding = bnodeMapping.getBinding(pattern.getObjectVar.getName)
        if (mappedObject != null) {
          `object` = mappedObject.getValue.asInstanceOf[Resource]
        } else {
          `object` = vf.createBNode
          bnodeMapping.addBinding(pattern.getObjectVar.getName, `object`)
        }
      }
    }
    if (pattern.getContextVar != null) {
      if (pattern.getContextVar.hasValue) {
        context = pattern.getContextVar.getValue.asInstanceOf[Resource]
      } else {
        context = sourceBinding.getValue(pattern.getContextVar.getName).asInstanceOf[Resource]
      }
    }
    var st: Statement = null
    if (subject != null && predicate != null && `object` != null) {
      if (context != null) {
        //todo: should one just ignore these or throw an error?
        //st = vf.createStatement(subject, predicate, `object`, context)
      } else {
        st = vf.createStatement(subject, predicate, `object`)
      }
    }
    return st
  }

  private def getValueForVar(va: Var, bindings: BindingSet): Node =
    if (va.hasValue()) {
      Node.fromSesame(va.getValue());
    } else {
      Node.fromSesame(bindings.getValue(va.getName()));
    }

  def executeUpdate(graph: Plantain#Graph,
    queryw: Plantain#UpdateQuery,
    map: Map[String, Plantain#Node]): Try[Plantain#Graph] = {
    import scala.collection.convert.wrapAsScala._
    Try {
      val bindingSet = map.asSesame
      val query = p.parseUpdate(queryw.query, "http://todo.example/")

      //implementation based on  org.openrdf.sail.helpers.SailUpdateExecutor
      //note this calls org.openrdf.sail.nativerdf.NativeStoreConnection  which uses optimisation strategies
      //sparql update: http://www.w3.org/TR/sparql11-update/
      var resultGraph = graph
      for (exp <- query.getUpdateExprs.iterator()) {
        exp match {
          case add: InsertData => {
            val insExp = add.getInsertExpr
            val evStrat = new EvaluationStrategyImpl(graph, null, null)
            val it = evStrat.evaluate(insExp, bindingSet)
            while (it.hasNext) {
              val bs = it.next()
              val s = bs.getValue("subject").asInstanceOf[Resource]
              val p = bs.getValue("predicate").asInstanceOf[SesameURI]
              val o = bs.getValue("object").asInstanceOf[Value]
              val context = bs.getValue("context").asInstanceOf[Resource];
              if (context == null)
                resultGraph += Triple(Node.fromSesame(s), Node.fromSesame(p), Node.fromSesame(o))
            }
          }
          case del: DeleteData => {
            val delExp = del.getDeleteExpr
            val evStrat = new EvaluationStrategyImpl(graph, null, null)
            val it = evStrat.evaluate(delExp, bindingSet)
            while (it.hasNext) {
              val bs = it.next()
              val s = bs.getValue("subject").asInstanceOf[Resource]
              val p = bs.getValue("predicate").asInstanceOf[SesameURI]
              val o = bs.getValue("object").asInstanceOf[Value]
              val context = bs.getValue("context").asInstanceOf[Resource];
              if (context == null)
                resultGraph = resultGraph.removeExistingTriple(Triple(Node.fromSesame(s), Node.fromSesame(p), Node.fromSesame(o)))
            }
          }
          case modify: Modify => {
            val whereClause: QueryRoot = modify.getWhereExpr match {
              case qr: QueryRoot => qr
              case whereClause => new QueryRoot(whereClause)
            }
            val evStrat = new EvaluationStrategyImpl(graph, null, null)
            val it = evStrat.evaluate(whereClause, bindingSet)
            val it2 = for (bs <- it.toIterator) yield {
              //see SailUpateExecutor.evaluateWhereClause
              import scala.collection.convert.decorateAsScala._
              val uniqueBindings = bindingSet.getBindingNames.asScala -- bs.getBindingNames.asScala
              if (uniqueBindings.size > 0) {
                val mergedSet = new MapBindingSet();
                for (bindingName <- bs.getBindingNames()) {
                  mergedSet.addBinding(bs.getBinding(bindingName));
                }
                for (bindingName <- uniqueBindings) {
                  mergedSet.addBinding(bindingSet.getBinding(bindingName));
                }
                mergedSet
              } else {
                bs
              }
            }
            for (sourceBinding <- it2) {
              val smallerGraph = deleteBoundTriples(graph, sourceBinding, modify.getDeleteExpr());

              insertBoundTriples(smallerGraph, sourceBinding, modify.getInsertExpr());

            }
          }
        }
      }
      resultGraph
    }
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
      bindings foreach { case (name, value) => bindingSet.addBinding(name, value) }
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

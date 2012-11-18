package org.w3.banana.plantain

import org.w3.banana._
import org.openrdf.model.{ URI => SesameURI, _ }
import org.openrdf.model.impl._
import scala.collection.JavaConversions._
import info.aduna.iteration.CloseableIteration
import org.openrdf.query._
import org.openrdf.query.impl._
import scalaz.Id._
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl
import PlantainUtil._

object PlantainGraphSparqlEngine extends RDFGraphQuery[Plantain] {  

  def makeSparqlEngine(graph: Plantain#Graph): SparqlEngine[Plantain, Id] =
    new PlantainSparqlEngine(graph)

  class PlantainSparqlEngine(graph: Plantain#Graph) extends SparqlEngine[Plantain, Id] {

    def executeSelect(query: Plantain#SelectQuery, bindings: Map[String, Plantain#Node]): Plantain#Solutions = {
      val tupleExpr = query.getTupleExpr
      val evaluationStrategy = new EvaluationStrategyImpl(graph)
      val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
      results.toIterator
    }

    def executeConstruct(query: Plantain#ConstructQuery, bindings: Map[String, Plantain#Node]): Plantain#Graph = {
      val tupleExpr = query.getTupleExpr
      val evaluationStrategy = new EvaluationStrategyImpl(graph)
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

    def executeAsk(query: Plantain#AskQuery, bindings: Map[String, Plantain#Node]): Boolean = {
      val tupleExpr = query.getTupleExpr
      val evaluationStrategy = new EvaluationStrategyImpl(graph)
      val results = evaluationStrategy.evaluate(tupleExpr, bindings.asSesame)
      results.hasNext
    }

  }



}


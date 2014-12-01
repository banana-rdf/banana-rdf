package org.w3.banana.jena

import com.hp.hpl.jena.graph.{Graph => JenaGraph}
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.update.{GraphStoreFactory, UpdateExecutionFactory}
import org.w3.banana._

import scala.util.Try

/**
 * Treat a Graph as a Sparql Engine
 */
class JenaGraphSparqlEngine(implicit ops: RDFOps[Jena])
    extends SparqlEngine[Jena, Try, Jena#Graph] with SparqlUpdate[Jena, Try, Jena#Graph] {
  val querySolution = new util.QuerySolution(ops)

  def qexec(graph: Jena#Graph, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    if (bindings.isEmpty)
      QueryExecutionFactory.create(query, model)
    else
      QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
  }

  def executeSelect(graph: Jena#Graph, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Try[Jena#Solutions] =
    Try {
      qexec(graph, query, bindings).execSelect()
    }

  def executeConstruct(graph: Jena#Graph, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Try[Jena#Graph] =
    Try {
      val result = qexec(graph, query, bindings).execConstruct()
      result.getGraph()
    }

  def executeAsk(graph: Jena#Graph, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Try[Boolean] =
    Try {
      qexec(graph, query, bindings).execAsk()
    }

  def executeUpdate(graph: Jena#Graph, updateQuery: Jena#UpdateQuery, bindings: Map[String, Jena#Node]): Try[Jena#Graph] =
    Try {
      import ops._
      val newGraph = ops.makeGraph(graph.triples)
      val graphstore= GraphStoreFactory.create(newGraph)
      val u = if (bindings.isEmpty)
        UpdateExecutionFactory.create(updateQuery, graphstore)
      else
        UpdateExecutionFactory.create(updateQuery, graphstore, querySolution.getMap(bindings))
      u.execute()
      newGraph
    }
}

object JenaGraphSparqlEngine {
  def apply(implicit ops: RDFOps[Jena]) = new JenaGraphSparqlEngine()
}

/**
 * see
 * - https://jena.apache.org/documentation/tdb/datasets.html
 * - http://jena.apache.org/documentation/tdb/assembler.html#union-default-graph
 */
trait UnionGraph extends JenaGraphSparqlEngine {

  override def qexec(graph: Jena#Graph, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = super.qexec(graph, query, bindings)
    import com.hp.hpl.jena.tdb.TDB
    qe.getContext().set(TDB.symUnionDefaultGraph, true)
    qe
  }

}

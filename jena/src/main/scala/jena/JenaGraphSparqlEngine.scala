package org.w3.banana.jena

import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model._
import org.w3.banana._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Treat a Graph as a Sparql Engine
 * @param ec execution context to use. If not specified this will
 *           be run on the same thread. If you want to use a different execution context
 *           you must specify it explicitly. (it makes most sense usually to run this on the same thread, as the
 *           data is local ).
 */
class JenaGraphSparqlEngine(ec: ExecutionContext)(implicit ops: RDFOps[Jena])
    extends SparqlEngine[Jena, Jena#Graph] {
  implicit val eci = ec //make ec implicit only inside the body
  val querySolution = new util.QuerySolution(ops)

  def qexec(graph: Jena#Graph, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    if (bindings.isEmpty)
      QueryExecutionFactory.create(query, model)
    else
      QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
  }

  def executeSelect(graph: Jena#Graph, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] =
    Future {
      qexec(graph, query, bindings).execSelect()
    }

  def executeConstruct(graph: Jena#Graph, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] =
    Future {
      val result = qexec(graph, query, bindings).execConstruct()
      result.getGraph()
    }

  def executeAsk(graph: Jena#Graph, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] =
    Future {
      qexec(graph, query, bindings).execAsk()
    }

}

object JenaGraphSparqlEngine {
  def apply(implicit ops: RDFOps[Jena]) = new JenaGraphSparqlEngine(sameThreadExecutionContext)
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

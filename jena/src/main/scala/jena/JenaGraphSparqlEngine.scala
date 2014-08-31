package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.concurrent.Future
import org.w3.banana.util._

class JenaGraphSparqlEngine(ops: RDFOps[Jena])
    extends SparqlEngine[Jena, Jena#Graph] {

  val querySolution = new util.QuerySolution(ops)

  def qexec(graph: Jena#Graph, query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    if (bindings.isEmpty)
      QueryExecutionFactory.create(query, model)
    else
      QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
  }

  def executeSelect(graph: Jena#Graph, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] = immediate {
    qexec(graph, query, bindings).execSelect()
  }

  def executeConstruct(graph: Jena#Graph, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] = immediate {
    val result = qexec(graph, query, bindings).execConstruct()
    result.getGraph()
  }

  def executeAsk(graph: Jena#Graph, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] = immediate {
    qexec(graph, query, bindings).execAsk()
  }

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

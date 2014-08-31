package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.concurrent.Future
import scala.concurrent.Future.successful

class JenaSparqlEngine(ops: RDFOps[Jena], graph: Jena#Graph) extends SparqlEngine[Jena] {

  val querySolution = new util.QuerySolution(ops)

  def qexec(query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    if (bindings.isEmpty)
      QueryExecutionFactory.create(query, model)
    else
      QueryExecutionFactory.create(query, model, querySolution.getMap(bindings))
  }

  def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] = successful {
    qexec(query, bindings).execSelect()
  }

  def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] = successful {
    val result = qexec(query, bindings).execConstruct()
    result.getGraph()
  }

  def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] = successful {
    qexec(query, bindings).execAsk()
  }

}

class JenaSparqlGraph(ops: RDFOps[Jena]) extends SparqlGraph[Jena] {

  def apply(graph: Jena#Graph): SparqlEngine[Jena] =
    new JenaSparqlEngine(ops, graph)

}

/**
 * see
 * - https://jena.apache.org/documentation/tdb/datasets.html
 * - http://jena.apache.org/documentation/tdb/assembler.html#union-default-graph
 */
trait UnionGraph extends JenaSparqlEngine {

  override def qexec(query: Jena#Query, bindings: Map[String, Jena#Node]): QueryExecution = {
    val qe = super.qexec(query, bindings)
    import com.hp.hpl.jena.tdb.TDB
    qe.getContext().set(TDB.symUnionDefaultGraph, true)
    qe
  }

}

class JenaSparqlUnionGraph(ops: RDFOps[Jena]) extends SparqlGraph[Jena] {

  def apply(graph: Jena#Graph): SparqlEngine[Jena] =
    new JenaSparqlEngine(ops, graph) with UnionGraph

}

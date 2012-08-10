package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._

object JenaGraphQuery extends RDFGraphQuery[Jena] {

  def executeSelect(graph: Jena#Graph, query: Jena#SelectQuery): Jena#Solutions = {
    val model: Model = ModelFactory.createModelForGraph(graph.jenaGraph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val solutions = qexec.execSelect()
    solutions
  }

  def executeConstruct(graph: Jena#Graph, query: Jena#ConstructQuery): Jena#Graph = {
    val model: Model = ModelFactory.createModelForGraph(graph.jenaGraph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execConstruct()
    BareJenaGraph(result.getGraph())
  }

  def executeAsk(graph: Jena#Graph, query: Jena#AskQuery): Boolean = {
    val model: Model = ModelFactory.createModelForGraph(graph.jenaGraph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execAsk()
    result
  }

}

object OpenJenaGraphQuery extends OpenGraphQuery(JenaGraphQuery, JenaSPARQLOperations)

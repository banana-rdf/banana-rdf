package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _ }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._

trait JenaRDFGraphQuery extends RDFGraphQuery[Jena, JenaSPARQL] {

  def executeSelect(query: JenaSPARQL#SelectQuery): JenaSPARQL#Solutions = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val solutions = qexec.execSelect()
    solutions
  }

  def executeConstruct(query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execConstruct()
    result.getGraph()
  }
  
  def executeAsk(query: JenaSPARQL#AskQuery): Boolean = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execAsk()
    result
  }

}

case class JenaGraphQuery(val graph: Jena#Graph) extends JenaRDFGraphQuery

case class OpenJenaGraphQuery(val graph: Jena#Graph) extends JenaRDFGraphQuery with OpenGraphQuery[Jena,JenaSPARQL] {
  def ops = JenaSPARQLOperations
}
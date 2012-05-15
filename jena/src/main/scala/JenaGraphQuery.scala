package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._

case class JenaGraphQuery(graph: JenaGraph) extends RDFQuery[Jena, JenaSPARQL] {

  def executeSelectQuery(query: JenaSPARQL#SelectQuery): Iterable[JenaSPARQL#Row] = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    new Iterable[JenaSPARQL#Row] {
      def iterator = solutions.asScala
    }
  }

  def getNode(row: JenaSPARQL#Row, v: String): JenaNode = {
    val node: RDFNode = row.get(v)
    JenaGraphTraversal.toNode(node)
  }

  def executeConstructQuery(query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execConstruct()
    result.getGraph()
  }
  
  def executeAskQuery(query: JenaSPARQL#AskQuery): Boolean = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execAsk()
    result
  }

}

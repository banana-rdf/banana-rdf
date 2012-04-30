package org.w3.rdf.jena

import org.w3.rdf._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._

object JenaSparql extends Sparql[Jena] {

  type SelectQuery = Query

  type ConstructQuery = Query

  type AskQuery = Query

  type Row = QuerySolution

  def SelectQuery(query: String): SelectQuery = QueryFactory.create(query)
    
  def executeSelectQuery(graph: JenaGraph, query: SelectQuery): Iterable[Row] = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    new Iterable[Row] {
      def iterator = solutions.asScala
    }
  }

  def getNode(row: Row, v: String): JenaNode = {
    val node: RDFNode = row.get(v)
    JenaGraphTraversal.toNode(node)
  }

  def ConstructQuery(query: String): ConstructQuery = QueryFactory.create(query)

  def executeConstructQuery(graph: JenaGraph, query: ConstructQuery): JenaGraph = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execConstruct()
    result.getGraph()
  }
  
  def AskQuery(query: String): AskQuery = QueryFactory.create(query)

  def executeAskQuery(graph: JenaGraph, query: AskQuery): Boolean = {
    val model: Model = ModelFactory.createModelForGraph(graph)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)
    val result = qexec.execAsk()
    result
  }



}

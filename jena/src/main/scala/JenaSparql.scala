package org.w3.rdf.jena

import org.w3.rdf._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._

object JenaSparql extends Sparql[Jena] {

  type Select = Query

  type Row = QuerySolution

  def Select(query: String): Select = QueryFactory.create(query)
  
  def executeSelect(graph: JenaGraph, query: Select): Iterable[Row] = {
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

}

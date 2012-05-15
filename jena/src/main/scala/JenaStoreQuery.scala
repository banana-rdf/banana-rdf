package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic

case class JenaStoreQuery(store: DatasetGraph) extends RDFQuery[Jena, JenaSPARQL] {

  def executeSelectQuery(query: JenaSPARQL#SelectQuery): Iterable[JenaSPARQL#Row] = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
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
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execConstruct()
    result.getGraph()
  }
  
  def executeAskQuery(query: JenaSPARQL#AskQuery): Boolean = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execAsk()
    result
  }

}

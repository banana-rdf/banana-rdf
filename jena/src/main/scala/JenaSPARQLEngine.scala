package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import scalaz.{Right3, Middle3, Left3}


trait JenaSPARQLEngine extends SPARQLEngine[Jena, JenaSPARQL] {

  def store: DatasetGraph

  def executeSelect(query: JenaSPARQL#SelectQuery): Iterable[JenaSPARQL#Row] = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    new Iterable[JenaSPARQL#Row] {
      def iterator = solutions.asScala
    }
  }

  def executeConstruct(query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execConstruct()
    result.getGraph()
  }

  def executeAsk(query: JenaSPARQL#AskQuery): Boolean = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execAsk()
    result
  }


}

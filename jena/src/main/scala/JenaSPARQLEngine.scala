package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import scala.collection.JavaConverters._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import scalaz.{ Validation, Success, Failure }

object JenaSPARQLEngine {

  def toRow(qs: QuerySolution): Row[Jena] =
    new Row[Jena] {
      def apply(v: String): Validation[BananaException, Jena#Node] = {
        val node: RDFNode = qs.get(v)
        if (node == null)
          Failure(VarNotFound("var " + v + " not found in QuerySolution " + qs.toString))
        else
          Success(JenaGraphTraversal.toNode(node))
      }
      def vars: Iterable[String] = new Iterable[String] {
        def iterator = qs.varNames.asScala
      }
    }

}

trait JenaSPARQLEngine extends SPARQLEngine[Jena, JenaSPARQL] {

  def store: DatasetGraph

  def executeSelect(query: JenaSPARQL#SelectQuery): Iterable[Row[Jena]] = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    val rows = solutions.asScala map JenaSPARQLEngine.toRow
    new Iterable[Row[Jena]] {
      def iterator = rows
    }
  }

  def executeSelectPlain(query: JenaSPARQL#SelectQuery): JenaSPARQL#Answers = {
    val dataset = new GraphStoreBasic(store).toDataset
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    qexec.execSelect()
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

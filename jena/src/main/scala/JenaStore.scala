package org.w3.banana.jena

import org.w3.banana._
import JenaOperations._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import scalaz.{ Validation, Success, Failure }
import scala.collection.JavaConverters._

object JenaStore {

  def apply(dataset: Dataset): RDFStore[Jena, JenaSPARQL] = new JenaStore(dataset)

  def apply(dg: DatasetGraph): RDFStore[Jena, JenaSPARQL] = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset)
  }

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

class JenaStore(dataset: Dataset) extends RDFStore[Jena, JenaSPARQL] {

  val dg: DatasetGraph = dataset.asDatasetGraph

  def addNamedGraph(uri: Jena#URI, graph: Jena#Graph): Unit = {
    dg.addGraph(uri, graph)
  }

  def appendToNamedGraph(uri: Jena#URI, graph: Jena#Graph): Unit = {
    Graph.toIterable(graph) foreach { case Triple(s, p, o) =>
      dg.add(uri, s, p, o)
    }
  }

  def getNamedGraph(uri: Jena#URI): Jena#Graph = {
    dg.getGraph(uri)
  }

  def removeGraph(uri: Jena#URI): Unit = {
    dg.removeGraph(uri)
  }

  def executeSelect(query: JenaSPARQL#SelectQuery): Iterable[Row[Jena]] = {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val solutions: java.util.Iterator[QuerySolution] = qexec.execSelect()
    val rows = solutions.asScala map JenaStore.toRow
    new Iterable[Row[Jena]] {
      def iterator = rows
    }
  }

  def executeConstruct(query: JenaSPARQL#ConstructQuery): JenaGraph = {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execConstruct()
    result.getGraph()
  }

  def executeAsk(query: JenaSPARQL#AskQuery): Boolean = {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execAsk()
    result
  }

}


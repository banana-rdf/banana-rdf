package org.w3.banana.jena

import org.w3.banana._
import JenaOperations._
import JenaDiesel._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import scalaz.{ Validation, Success, Failure }
import scala.collection.JavaConverters._
import com.hp.hpl.jena.rdf.model.ModelFactory.createModelForGraph

object JenaStore {

  def apply(dataset: Dataset, defensiveCopy: Boolean): JenaStore =
    new JenaStore(dataset, defensiveCopy)

  def apply(dg: DatasetGraph, defensiveCopy: Boolean = false): JenaStore = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset, defensiveCopy)
  }

}

class JenaStore(dataset: Dataset, defensiveCopy: Boolean) extends RDFStore[Jena] {

  val supportsTransactions: Boolean = dataset.supportsTransactions()

  val dg: DatasetGraph = dataset.asDatasetGraph

  def readTransaction[T](body: => T): T = {
    if (supportsTransactions) {
      dataset.begin(ReadWrite.READ)
      try {
        body
      } finally {
        dataset.end()
      }
    } else {
      body
    }
  }

  def writeTransaction[T](body: => T): T = {
    if (supportsTransactions) {
      dataset.begin(ReadWrite.WRITE)
      try {
        val result = body
        dataset.commit()
        result
      } finally {
        dataset.end()
      }
    } else {
      body
    }
  }

  def appendToGraph(uri: Jena#URI, graph: Jena#Graph): Unit = writeTransaction {
    graphToIterable(graph) foreach { case Triple(s, p, o) =>
      dg.add(uri, s, p, o)
    }
  }

  def getGraph(uri: Jena#URI): Jena#Graph = readTransaction {
    val graph = dg.getGraph(uri)
    if (defensiveCopy)
      JenaUtil.copy(graph)
    else
      graph
  }

  def removeGraph(uri: Jena#URI): Unit = writeTransaction {
    dg.removeGraph(uri)
  }

  def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Jena#Solutions = readTransaction {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val solutions = qexec.execSelect()
    solutions
  }

  def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): JenaGraph = readTransaction {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execConstruct()
    result.getGraph()
  }

  def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Boolean = readTransaction {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execAsk()
    result
  }

}


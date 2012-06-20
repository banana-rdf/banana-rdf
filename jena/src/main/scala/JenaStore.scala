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
import com.hp.hpl.jena.rdf.model.ModelFactory.createModelForGraph

object JenaStore {

  def apply(dataset: Dataset): RDFStore[Jena, JenaSPARQL] = new JenaStore(dataset)

  def apply(dg: DatasetGraph): RDFStore[Jena, JenaSPARQL] = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset)
  }

}

class JenaStore(dataset: Dataset) extends RDFStore[Jena, JenaSPARQL] {

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

  def addNamedGraph(uri: Jena#URI, graph: Jena#Graph): Unit = writeTransaction {
    dg.addGraph(uri, graph)
  }

  def appendToNamedGraph(uri: Jena#URI, graph: Jena#Graph): Unit = writeTransaction {
    Graph.toIterable(graph) foreach { case Triple(s, p, o) =>
      dg.add(uri, s, p, o)
    }
  }

  def getNamedGraph(uri: Jena#URI): Jena#Graph = readTransaction {
    dg.getGraph(uri)
  }

  def removeGraph(uri: Jena#URI): Unit = writeTransaction {
    dg.removeGraph(uri)
  }

  def executeSelect(query: JenaSPARQL#SelectQuery): JenaSPARQL#Solutions = readTransaction {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val solutions = qexec.execSelect()
    solutions
  }

  def executeConstruct(query: JenaSPARQL#ConstructQuery): JenaGraph = readTransaction {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execConstruct()
    result.getGraph()
  }

  def executeAsk(query: JenaSPARQL#AskQuery): Boolean = readTransaction {
    val qexec: QueryExecution = QueryExecutionFactory.create(query, dataset)
    val result = qexec.execAsk()
    result
  }

}


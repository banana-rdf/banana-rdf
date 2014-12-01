package org.w3.banana.jena

import com.hp.hpl.jena.graph.{Graph => JenaGraph, Node => JenaNode}
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.update.UpdateAction
import org.w3.banana._

import scala.util.Try

class JenaDatasetStore(defensiveCopy: Boolean)(implicit ops: RDFOps[Jena], jenaUtil: JenaUtil) extends RDFStore[Jena, Try, Dataset] with SparqlUpdate[Jena, Try, Dataset] {

  /* Transactor */

  def r[T](dataset: Dataset, body: => T): Try[T] = Try {
    dataset.begin(ReadWrite.READ)
    try {
      val result = body
      result
    } finally dataset.end()
  }

  def rw[T](dataset: Dataset, body: => T): Try[T] = Try {
    dataset.begin(ReadWrite.WRITE)
    try {
      val result = body
      dataset.commit()
      result
    } finally dataset.end()
  }

  /* SparqlEngine */

  lazy val querySolution = new util.QuerySolution(ops)

  def executeSelect(dataset: Dataset, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Try[Jena#Solutions] = Try {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val solutions = qexec.execSelect()
    solutions
  }

  /** Executes a Construct query. */
  def executeConstruct(dataset: Dataset, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Try[Jena#Graph] = Try {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val result = qexec.execConstruct()
    result.getGraph()
  }

  /** Executes a Ask query. */
  def executeAsk(dataset: Dataset, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Try[Boolean] = Try {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val result = qexec.execAsk()
    result
  }

  def executeUpdate(dataset: Dataset, query: Jena#UpdateQuery, bindings: Map[String, Jena#Node]) = Try {
    if (bindings.isEmpty) {
      UpdateAction.execute(query, dataset)
      dataset
    }
    else
      throw new NotImplementedError("todo: how does one (can one?) set the bindings in a dataset in Jena?")
  }

  /* GraphStore */

  def appendToGraph(dataset: Dataset, uri: Jena#URI, graph: Jena#Graph): Try[Unit] = Try {
    val dg = dataset.asDatasetGraph
    ops.getTriples(graph).foreach {
      case ops.Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def removeTriples(dataset: Dataset, uri: Jena#URI, triples: Iterable[TripleMatch[Jena]]): Try[Unit] = Try {
    val dg = dataset.asDatasetGraph
    triples.foreach {
      case (s, p, o) =>
        dg.deleteAny(uri, s, p, o)
    }
  }

  def getGraph(dataset: Dataset, uri: Jena#URI): Try[Jena#Graph] = Try {
    val dg = dataset.asDatasetGraph
    val graph = dg.getGraph(uri)
    if (defensiveCopy)
      jenaUtil.copy(graph)
    else
      graph
  }

  def removeGraph(dataset: Dataset, uri: Jena#URI): Try[Unit] = Try {
    val dg = dataset.asDatasetGraph
    dg.removeGraph(uri)
  }

}

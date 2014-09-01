package org.w3.banana.jena

import com.hp.hpl.jena.graph.{Graph => JenaGraph, Node => JenaNode}
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import org.w3.banana._
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.util.Try

class JenaDatasetStore(defensiveCopy: Boolean)(implicit ops: RDFOps[Jena], jenaUtil: JenaUtil) extends RDFStore[Jena, Dataset] /* with SparqlUpdate[Jena, Dataset] */ {

  import scala.concurrent.ExecutionContext.Implicits.global

  /* Transactor */

  def r[T](dataset: Dataset, body: => T): Try[T] = Try {
    dataset.begin(ReadWrite.READ)
    val result = body
    dataset.end()
    result
  }

  def rw[T](dataset: Dataset, body: => T): Try[T] = Try {
    dataset.begin(ReadWrite.WRITE)
    val result = body
    dataset.commit()
    result
  }

  /* SparqlEngine */

  lazy val querySolution = new util.QuerySolution(ops)

  def executeSelect(dataset: Dataset, query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Future[Jena#Solutions] = Future {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val solutions = qexec.execSelect()
    solutions
  }

  /** Executes a Construct query. */
  def executeConstruct(dataset: Dataset, query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Future[Jena#Graph] = Future {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val result = qexec.execConstruct()
    result.getGraph()
  }

  /** Executes a Ask query. */
  def executeAsk(dataset: Dataset, query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Future[Boolean] = Future {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val result = qexec.execAsk()
    result
  }

  /* GraphStore */

  def appendToGraph(dataset: Dataset, uri: Jena#URI, graph: Jena#Graph): Future[Unit] = Future {
    val dg = dataset.asDatasetGraph
    ops.getTriples(graph).foreach { case ops.Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def removeTriples(dataset: Dataset, uri: Jena#URI, triples: Iterable[TripleMatch[Jena]]): Future[Unit] = Future {
    val dg = dataset.asDatasetGraph
    triples.foreach { case (s, p, o) =>
      dg.deleteAny(uri, s, p, o)
    }
  }

  def getGraph(dataset: Dataset, uri: Jena#URI): Future[Jena#Graph] = Future {
    val dg = dataset.asDatasetGraph
    val graph = dg.getGraph(uri)
    if (defensiveCopy)
      jenaUtil.copy(graph)
    else
      graph
  }

  def removeGraph(dataset: Dataset, uri: Jena#URI): Future[Unit] = Future {
    val dg = dataset.asDatasetGraph
    dg.removeGraph(uri)
  }

}

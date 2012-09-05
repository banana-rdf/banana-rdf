package org.w3.banana.jena

import org.w3.banana._
import JenaOperations._
import JenaDiesel._
//import JenaLDC._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode }
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import com.hp.hpl.jena.datatypes.{ TypeMapper, RDFDatatype }
import scalaz._
import scala.collection.JavaConverters._

object JenaStore {

  def apply(dataset: Dataset, defensiveCopy: Boolean): JenaStore =
    new JenaStore(dataset, defensiveCopy)

  def apply(dg: DatasetGraph, defensiveCopy: Boolean = false): JenaStore = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset, defensiveCopy)
  }

}

class JenaStore[M[_]](dataset: Dataset, defensiveCopy: Boolean) extends RDFStore[Jena] {

  // there is a huge performance impact when using transaction (READs are 3x slower)
  // it's fine to deactivate that if there is only one active thread with akka
  // we should find something clever here...
  val supportsTransactions: Boolean = dataset.supportsTransactions()

  val dg: DatasetGraph = dataset.asDatasetGraph

  lazy val querySolution = util.QuerySolution()

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

  def run[A](script: Free[LDC[Jena]#Command, A]): A = {
    script.resume fold (
      {
        case JenaLDC.Create(uri, a) => {
          appendToGraph(uri, emptyGraph)
          run(a)
        }
        case JenaLDC.Delete(uri, a) => {
          removeGraph(uri)
          run(a)
        }
        case JenaLDC.Get(uri, f) => {
          val graph = getGraph(uri)
          run(f(graph))
        }
        case JenaLDC.Append(uri, triples, a) => {
          appendToGraph(uri, makeGraph(triples))
          run(a)
        }
        case JenaLDC.Remove(uri, tripleMatches, a) => {
          patchGraph(uri, tripleMatches, emptyGraph)
          run(a)
        }
      },
      a => a
    )
  }

  def operationType[A](script: Free[LDC[Jena]#Command, A]): RW = {
    script.resume fold (
      {
        case JenaLDC.Get(_, f) => operationType(f(ops.emptyGraph))
        case _ => WRITE
      },
      _ => READ
    )
  }

  override def execute[A](script: Free[LDC[Jena]#Command, A]): A = {
    operationType(script) match {
      case READ => readTransaction(run(script))
      case WRITE => writeTransaction(run(script))
    }

  }

  def appendToGraph(uri: Jena#URI, graph: Jena#Graph): Unit = {
    graphToIterable(graph) foreach {
      case Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def patchGraph(uri: Jena#URI, delete: Iterable[TripleMatch[Jena]], insert: Jena#Graph): Unit = {
    delete foreach { case (s, p, o) =>
      dg.deleteAny(uri, s, p, o)
    }
    graphToIterable(insert) foreach {
      case Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def getGraph(uri: Jena#URI): Jena#Graph = {
    val graph = BareJenaGraph(dg.getGraph(uri))
    if (defensiveCopy)
      JenaUtil.copy(graph)
    else
      graph
  }

  def removeGraph(uri: Jena#URI): Unit = {
    dg.removeGraph(uri)
  }

  def executeSelect(query: Jena#SelectQuery, bindings: Map[String, Jena#Node]): Jena#Solutions = {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val solutions = qexec.execSelect()
    solutions
  }

  def executeConstruct(query: Jena#ConstructQuery, bindings: Map[String, Jena#Node]): Jena#Graph = {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val result = qexec.execConstruct()
    BareJenaGraph(result.getGraph())
  }

  def executeAsk(query: Jena#AskQuery, bindings: Map[String, Jena#Node]): Boolean = {
    val qexec: QueryExecution =
      if (bindings.isEmpty)
        QueryExecutionFactory.create(query, dataset)
      else
        QueryExecutionFactory.create(query, dataset, querySolution.getMap(bindings))
    val result = qexec.execAsk()
    result
  }

}


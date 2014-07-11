package org.w3.banana.jena

import com.hp.hpl.jena.graph.{Graph => JenaGraph, Node => JenaNode}
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic
import org.slf4j.LoggerFactory
import org.w3.banana._

import scala.concurrent._
import scala.util.Try
import scalaz.Free

object JenaStore {

  def apply(dataset: Dataset, defensiveCopy: Boolean)(implicit ops: RDFOps[Jena], jenaUtil: JenaUtil): JenaStore =
    new JenaStore(dataset, defensiveCopy, ops, jenaUtil)

  def apply(dg: DatasetGraph, defensiveCopy: Boolean = false)(implicit ops: RDFOps[Jena], jenaUtil: JenaUtil): JenaStore = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset, defensiveCopy)
  }

  val logger = LoggerFactory.getLogger(classOf[JenaStore])

}

class JenaStore(dataset: Dataset, defensiveCopy: Boolean, ops: RDFOps[Jena], jenaUtil: JenaUtil) extends RDFStore[Jena] {

  val supportsTransactions: Boolean = dataset.supportsTransactions()

  val dg: DatasetGraph = dataset.asDatasetGraph

  lazy val querySolution = new util.QuerySolution(ops)

  def shutdown(): Unit = {
    dataset.close()
  }

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

  def run[A](script: Free[({ type l[+x] = Command[Jena, x] })#l, A]): A = {
    script.resume fold (
      {
        case Create(uri, a) => {
          appendToGraph(uri, List.empty)
          run(a)
        }
        case Delete(uri, a) => {
          removeGraph(uri)
          run(a)
        }
        case Get(uri, k) => {
          val graph = getGraph(uri)
          run(k(graph))
        }
        case Append(uri, triples, a) => {
          appendToGraph(uri, triples)
          run(a)
        }
        case Remove(uri, tripleMatches, a) => {
          removeFromGraph(uri, tripleMatches)
          run(a)
        }
        case Select(query, bindings, k) => {
          val solutions = executeSelect(query, bindings)
          run(k(solutions))
        }
        case Construct(query, bindings, k) => {
          val graph = executeConstruct(query, bindings)
          run(k(graph))
        }
        case Ask(query, bindings, k) => {
          val b = executeAsk(query, bindings)
          run(k(b))
        }
        case Update(query, bindings, k) => {
          executeUpdate(query, bindings)
          run(k)
        }
      },
      a => a
    )
  }

  def operationType[A](script: Free[({ type l[+x] = Command[Jena, x] })#l, A]): RW = {
    script.resume fold (
      {
        case Get(_, k) => operationType(k(null))
        case Select(_, _, k) => operationType(k(null))
        case Construct(_, _, k) => operationType(k(null))
        case Ask(_, _, k) => operationType(k(false))
        case Update(_, _, k) => operationType(k)
        case _ => WRITE
      },
      _ => READ
    )
  }

  def execute[A](script: Free[({ type l[+x] = Command[Jena, x] })#l, A]): Future[A] = Try {
    operationType(script) match {
      case READ => readTransaction(run(script))
      case WRITE => writeTransaction(run(script))
    }
  }.asFuture

  def appendToGraph(uri: Jena#URI, triples: Iterable[Jena#Triple]): Unit = {
    triples foreach {
      case ops.Triple(s, p, o) =>
        dg.add(uri, s, p, o)
    }
  }

  def removeFromGraph(uri: Jena#URI, tripleMatches: Iterable[TripleMatch[Jena]]): Unit = {
    tripleMatches foreach {
      case (s, p, o) =>
        dg.deleteAny(uri, s, p, o)
    }
  }

  def getGraph(uri: Jena#URI): Jena#Graph = {
    val graph = dg.getGraph(uri)
    if (defensiveCopy)
      jenaUtil.copy(graph)
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
    result.getGraph()
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

  def executeUpdate(query: Jena#UpdateQuery, bindings: Map[String, Jena#Node]): Unit = {
    ???
  }

}


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

  def apply[M[_]](dataset: Dataset, defensiveCopy: Boolean)(implicit m: Monad[M]): JenaStore[M] =
    new JenaStore(dataset, defensiveCopy, m)

  def apply[M[_]](dg: DatasetGraph, defensiveCopy: Boolean = false)(implicit m: Monad[M]): JenaStore[M] = {
    val dataset = new GraphStoreBasic(dg).toDataset
    JenaStore(dataset, defensiveCopy)(m)
  }

}

class JenaStore[M[_]](dataset: Dataset, defensiveCopy: Boolean, m: Monad[M]) extends RDFStore[Jena, M] {

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

  def run[A](script: Free[({type l[+x] = Command[Jena, x]})#l, A]): A = {
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
      },
      a => a
    )
  }

  def operationType[A](script: Free[({type l[+x] = Command[Jena, x]})#l, A]): RW = {
    script.resume fold (
      {
        case Get(_, f) => operationType(f(ops.emptyGraph))
        case _ => WRITE
      },
      _ => READ
    )
  }

  override def execute[A](script: Free[({type l[+x] = Command[Jena, x]})#l, A]): M[A] = {
    operationType(script) match {
      case READ => readTransaction(m.point(run(script)))
      case WRITE => writeTransaction(m.point(run(script)))
    }

  }

  def appendToGraph(uri: Jena#URI, triples: Iterable[Jena#Triple]): Unit = {
    triples foreach { case Triple(s, p, o) =>
      dg.add(uri, s, p, o)
    }
  }

  def removeFromGraph(uri: Jena#URI, tripleMatches: Iterable[TripleMatch[Jena]]): Unit = {
    tripleMatches foreach { case (s, p, o) =>
      dg.deleteAny(uri, s, p, o)
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


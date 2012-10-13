package org.w3.banana.sesame

import org.w3.banana._
import SesameUtil.withConnection
import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.repository.sail._
import org.openrdf.repository.RepositoryResult
import scala.collection.JavaConversions._
import info.aduna.iteration.CloseableIteration
import org.openrdf.sail.SailException
import org.openrdf.query._
import org.openrdf.query.impl._
import org.openrdf.sail.memory.MemoryStore
import scalaz.Free
import scala.concurrent._
import java.util.concurrent.{ ExecutorService, Executors }

object SesameStore {

  def apply(repository: SailRepository): SesameStore =
    new SesameStore(repository)

  def appendToGraph(conn: SailRepositoryConnection, uri: Sesame#URI, triples: Iterable[Sesame#Triple]): Unit = {
    triples foreach { triple =>
      conn.add(triple, uri)
    }
  }

  def removeFromGraph(conn: SailRepositoryConnection, uri: Sesame#URI, tripleMatches: Iterable[TripleMatch[Sesame]]): Unit = {
    tripleMatches foreach {
      case (s, p, o) =>
        // I don't really know what else to do...
        // in Sesame, a Triple is not a (Node, Node, Node)
        conn.remove(s.asInstanceOf[Resource], p.asInstanceOf[URI], o, uri)
    }
  }

  def getGraph(conn: SailRepositoryConnection, uri: Sesame#URI): Sesame#Graph = {
    val graph = new GraphImpl
    val rr: RepositoryResult[Statement] = conn.getStatements(null, null, null, false, uri)
    while (rr.hasNext) {
      val s = rr.next()
      graph.add(s)
    }
    rr.close()
    graph
  }

  def removeGraph(conn: SailRepositoryConnection, uri: Sesame#URI): Unit = {
    conn.remove(null: Resource, null, null, uri)
  }

  /**
   * Watch out connection is not closed here and neither is iterator.
   * (what does that mean? please help out)
   */
  def executeSelect(conn: SailRepositoryConnection, query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Sesame#Solutions = {
    val accumulator = new BindingsAccumulator()
    val tupleQuery: TupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
    tupleQuery.evaluate(accumulator)
    accumulator.bindings()
  }

  def executeConstruct(conn: SailRepositoryConnection, query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Sesame#Graph = {
    val graphQuery: GraphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => graphQuery.setBinding(name, value) }
    val result: GraphQueryResult = graphQuery.evaluate()
    val graph = new GraphImpl
    while (result.hasNext) {
      graph.add(result.next())
    }
    result.close()
    graph
  }

  def executeAsk(conn: SailRepositoryConnection, query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Boolean = {
    val booleanQuery: BooleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => booleanQuery.setBinding(name, value) }
    val result: Boolean = booleanQuery.evaluate()
    result
  }

}

import SesameStore._

class SesameStore(repository: SailRepository) extends RDFStore[Sesame, Future] {

  val executorService: ExecutorService = Executors.newFixedThreadPool(8)

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

  def shutdown(): Unit = {
    repository.shutDown()
  }

  def run[A](conn: SailRepositoryConnection, script: Free[({ type l[+x] = Command[Sesame, x] })#l, A]): A = {
    script.resume fold (
      {
        case Create(uri, a) => {
          appendToGraph(conn, uri, List.empty)
          run(conn, a)
        }
        case Delete(uri, a) => {
          removeGraph(conn, uri)
          run(conn, a)
        }
        case Get(uri, k) => {
          val graph = getGraph(conn, uri)
          run(conn, k(graph))
        }
        case Append(uri, triples, a) => {
          appendToGraph(conn, uri, triples)
          run(conn, a)
        }
        case Remove(uri, tripleMatches, a) => {
          removeFromGraph(conn, uri, tripleMatches)
          run(conn, a)
        }
        case Select(query, bindings, k) => {
          val solutions = executeSelect(conn, query, bindings)
          run(conn, k(solutions))
        }
        case Construct(query, bindings, k) => {
          val graph = executeConstruct(conn, query, bindings)
          run(conn, k(graph))
        }
        case Ask(query, bindings, k) => {
          val b = executeAsk(conn, query, bindings)
          run(conn, k(b))
        }
      },
      a => a
    )
  }

  def execute[A](script: Free[({ type l[+x] = Command[Sesame, x] })#l, A]): Future[A] = {
    def result = {
      val conn = repository.getConnection()
      val result: A = run(conn, script)
      conn.commit()
      conn.close()
      result
    }
    result.asFuture
  }

}


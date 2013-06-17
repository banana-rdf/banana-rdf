package org.w3.banana.sesame

import scala.collection.JavaConverters._
import scala.concurrent._

import java.util.concurrent.{ ExecutorService, Executors }

import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.repository.{Repository, RepositoryConnection, RepositoryResult}
import org.openrdf.query._

import scalaz.Free
import org.w3.banana._

object SesameStore {

  def apply(repository: Repository): SesameStore =
    new SesameStore(repository)

  def appendToGraph(conn: RepositoryConnection, uri: Sesame#URI, triples: Iterable[Sesame#Triple]): Unit = {
    conn.add(triples.asJava, uri)
  }

  def removeFromGraph(conn: RepositoryConnection, uri: Sesame#URI, tripleMatches: Iterable[TripleMatch[Sesame]]): Unit = {
    val ts = tripleMatches map { case (s, p, o) =>
      // I don't really know what else to do...
      // in Sesame, a Triple is not a (Node, Node, Node)
      new StatementImpl(s.asInstanceOf[Resource], p.asInstanceOf[URI], o)
    }
    conn.remove(ts.asJava, uri)
  }

  def getGraph(conn: RepositoryConnection, uri: Sesame#URI): Sesame#Graph = {
    val graph = new LinkedHashModel
    val rr: RepositoryResult[Statement] = conn.getStatements(null, null, null, false, uri)
    while (rr.hasNext) {
      val s = rr.next()
      graph.add(s)
    }
    rr.close()
    graph
  }

  def removeGraph(conn: RepositoryConnection, uri: Sesame#URI): Unit = {
    conn.remove(null: Resource, null, null, uri)
  }

  /**
   * Watch out connection is not closed here and neither is iterator.
   * (what does that mean? please help out)
   */
  def executeSelect(conn: RepositoryConnection, query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Sesame#Solutions = {
    val accumulator = new BindingsAccumulator()
    val tupleQuery: TupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
    tupleQuery.evaluate(accumulator)
    accumulator.bindings()
  }

  def executeConstruct(conn: RepositoryConnection, query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Sesame#Graph = {
    val graphQuery: GraphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => graphQuery.setBinding(name, value) }
    val result: GraphQueryResult = graphQuery.evaluate()
    val graph = new LinkedHashModel
    while (result.hasNext) {
      graph.add(result.next())
    }
    result.close()
    graph
  }

  def executeAsk(conn: RepositoryConnection, query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Boolean = {
    val booleanQuery: BooleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => booleanQuery.setBinding(name, value) }
    val result: Boolean = booleanQuery.evaluate()
    result
  }

  def executeUpdate(conn: RepositoryConnection, query: Sesame#UpdateQuery, bindings: Map[String, Sesame#Node]) {
    val updateQuery = conn.prepareUpdate(QueryLanguage.SPARQL, query.query)
    bindings foreach { case (name, value) => updateQuery.setBinding(name, value) }
    updateQuery.execute()
  }
}

import SesameStore._

class SesameStore(repository: Repository) extends RDFStore[Sesame, Future] {

  val executorService: ExecutorService = Executors.newFixedThreadPool(8)

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

  def shutdown(): Unit = {
    repository.shutDown()
  }

  def run[A](conn: RepositoryConnection, script: Free[({ type l[+x] = Command[Sesame, x] })#l, A]): A = {
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
        case org.w3.banana.Update(query, bindings, k) => {
          executeUpdate(conn, query, bindings)
          run(conn, k)
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


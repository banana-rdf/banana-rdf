package org.w3.banana.sesame

import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.query._
import org.openrdf.repository.{RepositoryConnection, RepositoryResult}
import org.w3.banana._

import scala.collection.JavaConverters._
import scala.util.Try

class SesameStore
    extends RDFStore[Sesame, Try, RepositoryConnection]
    with SparqlUpdate[Sesame, Try, RepositoryConnection] {

  /* Transactor */

  def r[T](conn: RepositoryConnection, body: => T): Try[T] = ???

  def rw[T](conn: RepositoryConnection, body: => T): Try[T] = ???

  /* SparqlEngine */

  /**
   * Watch out connection is not closed here and neither is iterator.
   * (what does that mean? please help out)
   */
  def executeSelect(conn: RepositoryConnection, query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Try[Sesame#Solutions] = Try {
    val accumulator = new BindingsAccumulator()
    val tupleQuery: TupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
    tupleQuery.evaluate(accumulator)
    accumulator.bindings()
  }

  def executeConstruct(conn: RepositoryConnection,
                       query: Sesame#ConstructQuery,
                       bindings: Map[String, Sesame#Node]): Try[Sesame#Graph] = Try {
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

  def executeAsk(conn: RepositoryConnection, query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Try[Boolean] = Try {
    val booleanQuery: BooleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => booleanQuery.setBinding(name, value) }
    val result: Boolean = booleanQuery.evaluate()
    result
  }

  def executeUpdate(conn: RepositoryConnection, query: Sesame#UpdateQuery, bindings: Map[String, Sesame#Node]): Try[RepositoryConnection] = Try {
    val updateQuery = conn.prepareUpdate(QueryLanguage.SPARQL, query.query)
    bindings foreach { case (name, value) => updateQuery.setBinding(name, value) }
    updateQuery.execute()
    conn
  }

  /* GraphStore */

  def appendToGraph(conn: RepositoryConnection, uri: Sesame#URI, graph: Sesame#Graph): Try[Unit] = Try {
    import org.w3.banana.sesame.Sesame.ops._
    val triples = graph.triples.to[Iterable].asJava
    conn.add(triples, uri)
  }

  def removeTriples(conn: RepositoryConnection, uri: Sesame#URI, tripleMatches: Iterable[TripleMatch[Sesame]]): Try[Unit] = Try {
    val ts = tripleMatches.map {
      case (s, p, o) =>
        new StatementImpl(s.asInstanceOf[Resource], p.asInstanceOf[URI], o)
    }
    conn.remove(ts.asJava, uri)
  }

  def getGraph(conn: RepositoryConnection, uri: Sesame#URI): Try[Sesame#Graph] = Try {
    val graph = new LinkedHashModel
    val rr: RepositoryResult[Statement] = conn.getStatements(null, null, null, false, uri)
    while (rr.hasNext) {
      val s = rr.next()
      graph.add(s)
    }
    rr.close()
    graph
  }

  def removeGraph(conn: RepositoryConnection, uri: Sesame#URI): Try[Unit] = Try {
    conn.remove(null: Resource, null, null, uri)
  }

}


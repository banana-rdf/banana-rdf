package org.w3.banana.rdf4j

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl._
import org.eclipse.rdf4j.query._
import org.eclipse.rdf4j.repository.{RepositoryConnection, RepositoryResult}
import org.w3.banana._

import scala.jdk.CollectionConverters._
import scala.util.Try

class Rdf4jStore
    extends RDFStore[Rdf4j, Try, RepositoryConnection]
    with SparqlUpdate[Rdf4j, Try, RepositoryConnection] {

  val valueFactory: ValueFactory = SimpleValueFactory.getInstance()

  /* Transactor */

  def r[T](conn: RepositoryConnection, body: => T): Try[T] = ???

  def rw[T](conn: RepositoryConnection, body: => T): Try[T] = ???

  /* SparqlEngine */

  /**
   * Watch out connection is not closed here and neither is iterator.
   * (what does that mean? please help out)
   */
  def executeSelect(conn: RepositoryConnection, query: Rdf4j#SelectQuery, bindings: Map[String, Rdf4j#Node]): Try[Rdf4j#Solutions] = Try {
    val accumulator = new BindingsAccumulator()
    val tupleQuery: TupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
    tupleQuery.evaluate(accumulator)
    accumulator.bindings()
  }

  def executeConstruct(conn: RepositoryConnection,
                       query: Rdf4j#ConstructQuery,
                       bindings: Map[String, Rdf4j#Node]): Try[Rdf4j#Graph] = Try {
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

  def executeAsk(conn: RepositoryConnection, query: Rdf4j#AskQuery, bindings: Map[String, Rdf4j#Node]): Try[Boolean] = Try {
    val booleanQuery: BooleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query.getSourceString)
    bindings foreach { case (name, value) => booleanQuery.setBinding(name, value) }
    val result: Boolean = booleanQuery.evaluate()
    result
  }

  def executeUpdate(conn: RepositoryConnection, query: Rdf4j#UpdateQuery, bindings: Map[String, Rdf4j#Node]): Try[Unit] = Try {
    val updateQuery = conn.prepareUpdate(QueryLanguage.SPARQL, query.query)
    bindings foreach { case (name, value) => updateQuery.setBinding(name, value) }
    updateQuery.execute()
  }

  /* GraphStore */

  def appendToGraph(conn: RepositoryConnection, uri: Rdf4j#URI, graph: Rdf4j#Graph): Try[Unit] = Try {
    import Rdf4j.ops._
    val triples = graph.triples.asJava
    conn.add(triples, uri)
  }

  def removeTriples(conn: RepositoryConnection, uri: Rdf4j#URI, tripleMatches: Iterable[TripleMatch[Rdf4j]]): Try[Unit] = Try {
    val ts = tripleMatches.map {
      case (s, p, o) =>
        valueFactory.createStatement(s.asInstanceOf[Resource], p.asInstanceOf[IRI], o)
    }
    conn.remove(ts.asJava, uri)
  }

  def getGraph(conn: RepositoryConnection, uri: Rdf4j#URI): Try[Rdf4j#Graph] = Try {
    val graph = new LinkedHashModel
    val rr: RepositoryResult[Statement] = conn.getStatements(null, null, null, false, uri)
    while (rr.hasNext) {
      val s = rr.next()
      graph.add(s)
    }
    rr.close()
    graph
  }

  def removeGraph(conn: RepositoryConnection, uri: Rdf4j#URI): Try[Unit] = Try {
    conn.remove(null: Resource, null, null, uri)
  }

}


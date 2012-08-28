package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import SesameUtil.withConnection
import org.openrdf.repository.sail.SailRepository
import org.openrdf.repository.RepositoryResult
import scala.collection.JavaConversions._
import info.aduna.iteration.CloseableIteration
import org.openrdf.sail.SailException
import org.openrdf.query._
import org.openrdf.query.impl._
import org.openrdf.sail.memory.MemoryStore

object SesameStore {

  def apply(store: SailRepository): RDFStore[Sesame] =
    new SesameStore(store)

  implicit def toMemoryStore(graph: Sesame#Graph) = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.initialize()
    val sailconn = sail.getConnection
    sailconn.add(graph)
    SesameStore(sail)
  }
}

class SesameStore(store: SailRepository) extends RDFStore[Sesame] {

  def appendToGraph(uri: Sesame#URI, graph: Sesame#Graph): Unit = {
    withConnection(store) { conn =>
      for (s: Statement <- graph.`match`(null, null, null))
        conn.add(s, uri)
    }
  }

  def patchGraph(uri: Sesame#URI, delete: Iterable[TripleMatch[Sesame]], insert: Sesame#Graph): Unit = {
    withConnection(store) { conn =>
      delete foreach {
        case (s, p, o) =>
          // I don't really know what else to do...
          // in Sesame, a Triple is not a (Node, Node, Node)
          conn.remove(s.asInstanceOf[Resource], p.asInstanceOf[URI], o, uri)
      }
      for (s: Statement <- insert.`match`(null, null, null))
        conn.add(s, uri)
    }
  }

  def getGraph(uri: Sesame#URI): Sesame#Graph = {
    val graph = new GraphImpl
    withConnection(store) { conn =>
      val rr: RepositoryResult[Statement] = conn.getStatements(null, null, null, false, uri)
      while (rr.hasNext) {
        val s = rr.next()
        graph.add(s)
      }
      rr.close()
    }
    graph
  }

  def removeGraph(uri: Sesame#URI): Unit = {
    withConnection(store) { conn =>
      conn.remove(null: Resource, null, null, uri)
    }
  }

  val TODO = "http://w3.org/TODO#"

  val empty = new EmptyBindingSet()

  /**
   * Watch out connection is not closed here and neither is iterator.
   * (what does that mean? please help out)
   * @param query
   * @return
   */
  def executeSelect(query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Sesame#Solutions = {
    withConnection(store) { conn =>
      val tupleQuery: TupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.getSourceString)
      bindings foreach { case (name, value) => tupleQuery.setBinding(name, value) }
      val result: TupleQueryResult = tupleQuery.evaluate()
      result
    }
  }

  def executeConstruct(query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Sesame#Graph =
    withConnection(store) { conn =>
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

  def executeAsk(query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Boolean =
    withConnection(store) { conn =>
      val booleanQuery: BooleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query.getSourceString)
      bindings foreach { case (name, value) => booleanQuery.setBinding(name, value) }
      val result: Boolean = booleanQuery.evaluate()
      result
    }

}


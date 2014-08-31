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
import scala.concurrent.Future

class SesameGraphSparqlEngine extends SparqlEngine[Sesame, Sesame#Graph] {

  val store = new SesameStore

  def asConn(graph: Sesame#Graph) = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.initialize()
    withConnection(sail) { conn =>
      conn.add(graph)
    }
    sail.getConnection()
  }

  def executeSelect(graph: Sesame#Graph, query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Future[Sesame#Solutions] =
    store.executeSelect(asConn(graph), query, bindings)

    def executeConstruct(graph: Sesame#Graph, query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Future[Sesame#Graph] =
    store.executeConstruct(asConn(graph), query, bindings)

    def executeAsk(graph: Sesame#Graph, query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Future[Boolean] =
      store.executeAsk(asConn(graph), query, bindings)
    
}

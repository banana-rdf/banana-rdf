package org.w3.banana.sesame

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.w3.banana._
import org.w3.banana.sesame.SesameUtil.withConnection

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Treat a Graph as a Sparql Engine
 * @param ec execution context to use. If not specified this will
 *           be run on the same thread. If you want to use a different execution context
 *           you must specify it explicitly.
 */
class SesameGraphSparqlEngine(ec: ExecutionContext) extends SparqlEngine[Sesame, Sesame#Graph] {

  val store = new SesameStore()(ec)

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

object SesameGraphSparqlEngine {
  def apply() = new SesameGraphSparqlEngine(sameThreadExecutionContext)
}

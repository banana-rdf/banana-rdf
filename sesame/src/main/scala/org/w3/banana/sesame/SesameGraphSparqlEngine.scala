package org.w3.banana.sesame

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.w3.banana._
import org.w3.banana.sesame.SesameUtil.withConnection

import scala.util.Try

/**
 * Treat a Graph as a Sparql Engine
 */
class SesameGraphSparqlEngine extends SparqlEngine[Sesame, Try, Sesame#Graph] {

  val store = new SesameStore()

  def asConn(graph: Sesame#Graph) = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.initialize()
    withConnection(sail) { conn =>
      conn.add(graph)
    }
    sail.getConnection()
  }

  def executeSelect(graph: Sesame#Graph, query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Try[Sesame#Solutions] =
    store.executeSelect(asConn(graph), query, bindings)

  def executeConstruct(graph: Sesame#Graph, query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Try[Sesame#Graph] =
    store.executeConstruct(asConn(graph), query, bindings)

  def executeAsk(graph: Sesame#Graph, query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Try[Boolean] =
    store.executeAsk(asConn(graph), query, bindings)

}

object SesameGraphSparqlEngine {
  def apply() = new SesameGraphSparqlEngine()
}

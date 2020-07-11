package org.w3.banana.rdf4j

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.w3.banana._
import org.w3.banana.rdf4j.Rdf4jUtil.withConnection

import scala.util.Try

/**
 * Treat a Graph as a Sparql Engine
 */
class Rdf4jGraphSparqlEngine extends SparqlEngine[Rdf4j, Try, Rdf4j#Graph] {

  val store = new Rdf4jStore()

  def asConn(graph: Rdf4j#Graph) = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.init()
    withConnection(sail) { conn =>
      conn.add(graph)
    }
    sail.getConnection()
  }

  def executeSelect(graph: Rdf4j#Graph, query: Rdf4j#SelectQuery, bindings: Map[String, Rdf4j#Node]): Try[Rdf4j#Solutions] =
    store.executeSelect(asConn(graph), query, bindings)

  def executeConstruct(graph: Rdf4j#Graph, query: Rdf4j#ConstructQuery, bindings: Map[String, Rdf4j#Node]): Try[Rdf4j#Graph] =
    store.executeConstruct(asConn(graph), query, bindings)

  def executeAsk(graph: Rdf4j#Graph, query: Rdf4j#AskQuery, bindings: Map[String, Rdf4j#Node]): Try[Boolean] =
    store.executeAsk(asConn(graph), query, bindings)

}

object Rdf4jGraphSparqlEngine {
  def apply() = new Rdf4jGraphSparqlEngine()
}

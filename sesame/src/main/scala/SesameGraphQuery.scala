package org.w3.banana.sesame

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.sail.Sail
import org.w3.banana.{OpenGraphQuery, Row, RDFGraphQuery}
import org.w3.banana.sesame.{SesameStore, Sesame, SesameSPARQL}
import scalaz.Failure

/**
 * Sesame Graph Queries are better made on stores, if multiple queries need
 * to be made on the same graph. This class needs to map the graph to the store
 * for each query.
 *
 * arguably SesameGraphQuery should be a case class
 *    SesameGraphQuery(graph)
 * and indeed this should extend to RDFGraphQuery(graph)
 *
 */
trait SesameRDFGraphQuery extends RDFGraphQuery[Sesame, SesameSPARQL] {

  /**
   * Sesame queries can only be made on stores, hence this is needed.
   * @param graph
   */
  lazy val store = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.initialize()
    val sailconn = sail.getConnection
    sailconn.add(graph)
    SesameStore(sail)
  }

  def executeSelect(query: SesameSPARQL#SelectQuery): Iterable[Row[Sesame]] = {
    store.executeSelect(query)
  }

  def executeConstruct(query: SesameSPARQL#ConstructQuery) = {
    store.executeConstruct(query)
  }

  def executeAsk(query: SesameSPARQL#AskQuery) = {
    store.executeAsk(query)
  }

  /**
   * This returns the underlying objects, which is useful when needing to serialise the answer
   * for example
   * @param query
   * @return
   */
  def executeSelectPlain(query: SesameSPARQL#SelectQuery) = store.executeSelectPlain(query)
}

case class SesameGraphQuery(graph: Sesame#Graph) extends SesameRDFGraphQuery

case class OpenSesameGraphQuery(graph: Sesame#Graph)
  extends SesameRDFGraphQuery
  with OpenGraphQuery[Sesame, SesameSPARQL] {
  def ops = SesameSPARQLOperations
}

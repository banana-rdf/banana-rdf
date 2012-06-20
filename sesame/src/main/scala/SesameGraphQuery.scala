package org.w3.banana.sesame

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.w3.banana.{OpenGraphQuery, RDFGraphQuery}

/**
 * Sesame Graph Queries are better made on stores, if multiple queries need
 * to be made on the same graph. This class needs to map the graph to the store
 * for each query.
 * ?? Sesame has ways to add warnings to compilations...
 */
object SesameGraphQuery extends RDFGraphQuery[Sesame, SesameSPARQL] {

  /**
   * Sesame queries can only be made on stores, hence this is needed.
   * @param graph
   */
  protected def makeStore(graph: Sesame#Graph) = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.initialize()
    val sailconn = sail.getConnection
    sailconn.add(graph)
    SesameStore(sail)
  }

  def executeSelect(graph: Sesame#Graph, query: SesameSPARQL#SelectQuery) = {
    makeStore(graph).executeSelect(query)
  }

  def executeConstruct(graph: Sesame#Graph, query: SesameSPARQL#ConstructQuery) = {
    makeStore(graph).executeConstruct(query)
  }

  def executeAsk(graph: Sesame#Graph, query: SesameSPARQL#AskQuery) = {
    makeStore(graph).executeAsk(query)

  }
}

object OpenSesameGraphQuery extends OpenGraphQuery(SesameGraphQuery,SesameSPARQLOperations)


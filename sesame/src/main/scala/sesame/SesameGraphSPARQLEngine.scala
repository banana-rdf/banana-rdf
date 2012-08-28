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

object SesameGraphSPARQLEngine extends RDFGraphQuery[Sesame] {

  def makeSPARQLEngine(graph: Sesame#Graph): SPARQLEngine[Sesame] = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    sail.initialize()
    val sailconn = sail.getConnection
    sailconn.add(graph)
    SesameStore(sail)
  }

}

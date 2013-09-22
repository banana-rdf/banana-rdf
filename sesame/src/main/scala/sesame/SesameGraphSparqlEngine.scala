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

object SesameSparqlGraph extends SesameSparqlGraph

trait SesameSparqlGraph extends SparqlGraph[Sesame] {

  def apply(graph: Sesame#Graph): SparqlEngine[Sesame] = new SparqlEngine[Sesame] {

    val repository = {
      val store = new MemoryStore
      val sail = new SailRepository(store)
      sail.initialize()
      withConnection(sail) { conn =>
        conn.add(graph)
      }
      sail
    }

    def executeSelect(query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Future[Sesame#Solutions] =
      withConnection(repository) { conn => SesameStore.executeSelect(conn, query, bindings) }

    def executeConstruct(query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Future[Sesame#Graph] =
      withConnection(repository) { conn => SesameStore.executeConstruct(conn, query, bindings) }

    def executeAsk(query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Future[Boolean] =
      withConnection(repository) { conn => SesameStore.executeAsk(conn, query, bindings) }

  }

}

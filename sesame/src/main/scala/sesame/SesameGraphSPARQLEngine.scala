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
import scalaz.Id._

object SesameGraphSPARQLEngine extends RDFGraphQuery[Sesame] {

  def makeSPARQLEngine(graph: Sesame#Graph): SPARQLEngine[Sesame, Id] = new SPARQLEngine[Sesame, Id] {

    val repository = {
      val store = new MemoryStore
      val sail = new SailRepository(store)
      sail.initialize()
      withConnection(sail) { conn =>
        conn.add(graph)
      }
      sail
    }

    def executeSelect(query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Sesame#Solutions =
      withConnection(repository) { conn => SesameStore.executeSelect(conn, query, bindings) }

    def executeConstruct(query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Sesame#Graph =
      withConnection(repository) { conn => SesameStore.executeConstruct(conn, query, bindings) }

    def executeAsk(query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Boolean =
      withConnection(repository) { conn => SesameStore.executeAsk(conn, query, bindings) }

  }

}

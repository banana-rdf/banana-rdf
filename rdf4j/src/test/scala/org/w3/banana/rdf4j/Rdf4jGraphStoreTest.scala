package org.w3.banana.rdf4j

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.w3.banana._
import org.w3.banana.rdf4j.Rdf4j._
import org.w3.banana.util.tryInstances._

import scala.util.Try

abstract class Rdf4jGraphStoreTest(conn: RepositoryConnection) extends GraphStoreTest[Rdf4j, Try, RepositoryConnection](conn) {

  import graphStore.graphStoreSyntax._
  import ops._

  "adding a named graph should not pollute the default graph" in {
    val defaultGraph =
      conn.appendToGraph(makeUri("http://example.com/foo"), graph).flatMap { _ =>
        conn.getGraph(null.asInstanceOf[Rdf4j#URI])
      }.get
    defaultGraph should have size (0)
  }

}

class Rdf4jMemoryGraphStoreTest extends Rdf4jGraphStoreTest({
  val repo = new SailRepository(new MemoryStore)
  repo.init()
  repo.getConnection()
})


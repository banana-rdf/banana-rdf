package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.w3.banana._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import Sesame._

abstract class SesameGraphStoreTest(conn: RepositoryConnection) extends GraphStoreTest[Sesame, RepositoryConnection](conn) {

  import ops._
  import graphStore.graphStoreSyntax._

  "adding a named graph should not pollute the default graph" in {
    val defaultGraph =
      conn.appendToGraph(makeUri("http://example.com/foo"), graph).flatMap { _ =>
        conn.getGraph(null.asInstanceOf[Sesame#URI])
      }.getOrFail()
    defaultGraph should have size (0)
  }

}

class SesameMemoryGraphStoreTest extends SesameGraphStoreTest({
  val repo = new SailRepository(new MemoryStore)
  repo.initialize()
  repo.getConnection()
})


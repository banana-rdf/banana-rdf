package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import Sesame._

class SesameGraphStoreTest() extends GraphStoreTest[Sesame](
  SesameStore {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  }) {

  import SesameOperations._
  import store._

  "adding a named graph should not pollute the default graph" in {
    appendToGraph(makeUri("http://example.com/foo"), graph)
    val defaultGraph = getGraph(null.asInstanceOf[Sesame#URI])
    defaultGraph must have size (0)
  }

}

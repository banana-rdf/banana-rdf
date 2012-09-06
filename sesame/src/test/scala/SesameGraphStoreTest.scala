package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import Sesame._
import SesameOperations._

abstract class SesameGraphStoreTest(sesameStore: SesameStore) extends GraphStoreTest[Sesame](sesameStore) {

  "adding a named graph should not pollute the default graph" in {
    val s = sesameStore.execute {
      for {
        _ <- Command.append[Sesame](makeUri("http://example.com/foo"), graphToIterable(graph))
        graph <- Command.get[Sesame](null.asInstanceOf[Sesame#URI])
      } yield graph
    }
    val defaultGraph = s.getOrFail()
    defaultGraph must have size (0)
  }

}

class SesameMemoryGraphStoreTest extends SesameGraphStoreTest({
  SesameStore {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  }
})

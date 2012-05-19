package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository

class SesameGraphStoreTest() extends GraphStoreTest[Sesame](
  SesameOperations,
  SesameDiesel,
  SesameGraphUnion,
  SesameStore {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  },
  SesameRDFXMLReader,
  SesameGraphIsomorphism) {

  import SesameOperations._
  import store._

  "adding a named graph should not pollute the default graph" in {
    addNamedGraph(URI("http://example.com/foo"), graph)
    val defaultGraph = getNamedGraph(null.asInstanceOf[Sesame#URI])
    defaultGraph must have size(0)
  }

}

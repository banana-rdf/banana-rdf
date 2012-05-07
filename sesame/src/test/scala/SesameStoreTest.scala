package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository

class SesameStoreTest() extends StoreTest[Sesame](
  SesameOperations,
  SesameDiesel,
  SesameGraphUnion,
  SesameStore,
  SesameRDFXMLReader,
  SesameGraphIsomorphism) {

  val store: Repository = {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  }

  import SesameOperations._
  import SesameStore._

  "adding a named graph should not pollute the default graph" in {
    addNamedGraph(store, IRI("http://example.com/foo"), graph)
    val defaultGraph = getNamedGraph(store, null.asInstanceOf[Sesame#IRI])
    defaultGraph must have size(0)
  }

}

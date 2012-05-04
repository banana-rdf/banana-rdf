package org.w3.rdf.sesame

import org.w3.rdf._

import org.openrdf.sail._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository._
import org.openrdf.repository.sail._

class SesameStoreTest() extends StoreTest[Sesame](
  SesameOperations,
  SesameDiesel,
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

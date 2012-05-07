package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository

class SesameQueryOnStoreTest() extends SparqlQueryOnStoreTest(
  SesameOperations,
  SesameDiesel,
  SesameStore,
  SesameGraphIsomorphism,
  SesameQueryBuilder,
  SesameStoreQuery) {

  val store: Repository = {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  }

}

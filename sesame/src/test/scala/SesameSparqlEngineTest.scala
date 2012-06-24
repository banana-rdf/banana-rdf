package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import Sesame._

class SesameSparqlEngineTest extends SparqlEngineTest(
  SesameStore {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  })

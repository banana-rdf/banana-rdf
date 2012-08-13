package org.w3.banana.sesame

import org.w3.banana._
import Sesame._
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.sail.SailRepository

class SesameAsyncGraphStoreTest() extends AsyncGraphStoreTest[Sesame](
  SesameStore {
    val repo = new SailRepository(new MemoryStore)
    repo.initialize()
    repo
  })

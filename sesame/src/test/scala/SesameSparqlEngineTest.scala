package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.w3.banana._

class SesameSparqlEngineTest extends SparqlEngineTest[Sesame, RepositoryConnection]({
  val repo = new SailRepository(new MemoryStore)
  //    val d = java.nio.file.Files.createTempDirectory("sesame-")
  //    val repo = new SailRepository((new NativeStore(d.toFile, "spoc,posc")))
  repo.initialize()
  repo.getConnection()
})

class SesameSparqlEngineUpdateTest extends SparqlUpdateEngineTest[Sesame, RepositoryConnection]({
  val repo = new SailRepository(new MemoryStore)
  //    val d = java.nio.file.Files.createTempDirectory("sesame-")
  //    val repo = new SailRepository((new NativeStore(d.toFile, "spoc,posc")))
  repo.initialize()
  repo.getConnection()
})
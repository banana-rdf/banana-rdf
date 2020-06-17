package org.w3.banana.rdf4j

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.w3.banana._
import org.w3.banana.util.tryInstances._

class Rdf4jSparqlEngineTest extends SparqlEngineTest[Rdf4j, RepositoryConnection]({
  val repo = new SailRepository(new MemoryStore)
  //    val d = java.nio.file.Files.createTempDirectory("rdf4j-")
  //    val repo = new SailRepository((new NativeStore(d.toFile, "spoc,posc")))
  repo.initialize()
  repo.getConnection()
})

class Rdf4jSparqlEngineUpdateTest extends SparqlUpdateEngineTest[Rdf4j, RepositoryConnection]({
  val repo = new SailRepository(new MemoryStore)
  //    val d = java.nio.file.Files.createTempDirectory("rdf4j-")
  //    val repo = new SailRepository((new NativeStore(d.toFile, "spoc,posc")))
  repo.initialize()
  repo.getConnection()
})
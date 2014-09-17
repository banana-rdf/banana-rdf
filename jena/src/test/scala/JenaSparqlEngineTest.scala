package org.w3.banana.jena

import com.hp.hpl.jena.query.DatasetFactory
import org.w3.banana._
import org.w3.banana.jena.Jena._

class JenaSparqlEngineTest extends SparqlEngineTest({
  DatasetFactory.createMem()
})

class JenaSparqlUpdateEngineTest extends SparqlUpdateEngineTest({
  DatasetFactory.createMem()
})

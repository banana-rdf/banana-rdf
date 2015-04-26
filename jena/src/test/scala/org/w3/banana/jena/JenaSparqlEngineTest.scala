package org.w3.banana.jena

import org.apache.jena.query.DatasetFactory
import org.w3.banana._
import org.w3.banana.jena.Jena._
import org.w3.banana.util.tryInstances._


class JenaSparqlEngineTest extends SparqlEngineTest({
  DatasetFactory.createGeneral()
})

class JenaSparqlUpdateEngineTest extends SparqlUpdateEngineTest({
  DatasetFactory.createGeneral()
})

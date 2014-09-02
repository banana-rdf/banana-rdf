package org.w3.banana.jena

import com.hp.hpl.jena.query.DatasetFactory
import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import JenaRDFReader._

class JenaSparqlEngineTest extends SparqlEngineTest({
  DatasetFactory.createMem()
})


package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._

class JenaSparqlEngineTest() extends SparqlEngineTest(
  JenaOperations,
  JenaDiesel,
  JenaGraphIsomorphism,
  JenaSPARQLOperations,
  JenaStore(DatasetGraphFactory.createMem()))

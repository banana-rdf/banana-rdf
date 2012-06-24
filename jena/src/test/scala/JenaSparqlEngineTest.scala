package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._

class JenaSparqlEngineTest() extends SparqlEngineTest(JenaStore(DatasetGraphFactory.createMem()))

  

package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._

class JenaAsyncSparqlEngineTest() extends AsyncSparqlEngineTest(
  JenaOperations,
  JenaRDFXMLReader,
  JenaDiesel,
  JenaGraphIsomorphism,
  JenaSPARQLOperations,
  JenaStore(DatasetGraphFactory.createMem()))

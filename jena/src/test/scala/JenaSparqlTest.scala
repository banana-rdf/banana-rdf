package org.w3.banana.jena

import org.w3.banana._

class JenaSparqlTest() extends SparqlTest(
  JenaOperations,
  JenaRDFXMLReader,
  JenaGraphIsomorphism,
  JenaQueryBuilder,
  JenaGraphQuery)

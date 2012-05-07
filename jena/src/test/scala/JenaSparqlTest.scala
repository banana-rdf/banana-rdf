package org.w3.rdf.jena

import org.w3.rdf._

class JenaSparqlTest() extends SparqlTest(
  JenaOperations,
  JenaRDFXMLReader,
  JenaGraphIsomorphism,
  JenaQueryBuilder,
  JenaGraphQuery)

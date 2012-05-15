package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._

class JenaAsyncQueryOnStoreTest() extends AsyncSparqlQueryOnStoreTest(
  JenaOperations,
  JenaDiesel,
  JenaGraphIsomorphism,
  JenaQueryBuilder,
  DatasetGraphFactory.createMem(),
  (u: DatasetGraph) => JenaStore(u),
  (u: DatasetGraph) => JenaStoreQuery(u))

package org.w3.rdf.jena

import org.w3.rdf._
import com.hp.hpl.jena.sparql.core._

class JenaQueryOnStoreTest() extends SparqlQueryOnStoreTest(
  JenaOperations,
  JenaDiesel,
  JenaStore,
  JenaGraphIsomorphism,
  JenaQueryBuilder,
  JenaStoreQuery) {

  val store: DatasetGraph = DatasetGraphFactory.createMem()

}

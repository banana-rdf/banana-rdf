package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import JenaRDFReader._
import com.hp.hpl.jena.tdb.{TDB, TDBFactory}


abstract class AbstractJenaGraphStoreTest(val jenaStore: JenaStore) extends GraphStoreTest[Jena](jenaStore) {

  import JenaOperations._

  def defaultGraph: Jena#Graph = jenaStore.readTransaction {
    BareJenaGraph(jenaStore.dg.getDefaultGraph)
  }

  "adding a named graph should not pollute the default graph" in {
    store.appendToGraph(makeUri("http://example.com/foo"), graph)
    defaultGraph.jenaGraph must have size(0)
  }

}

class JenaMemGraphStore extends AbstractJenaGraphStoreTest(JenaStore(DatasetGraphFactory.createMem()))

class JenaTDBGraphStore extends AbstractJenaGraphStoreTest(JenaStore(TDBFactory.createDataset("test.tdb"), defensiveCopy = true)) {
  TDB.getContext().set(TDB.symUnionDefaultGraph, false)
}

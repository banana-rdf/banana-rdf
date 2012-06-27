package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import JenaRDFReader._
import com.hp.hpl.jena.tdb.{TDB, TDBFactory}


abstract class AbstractJenaGraphStoreTest(val jenaStore: JenaStore) extends GraphStoreTest[Jena](jenaStore) {

  import JenaOperations._

  def defaultGraph: Jena#Graph = jenaStore.readTransaction {
    jenaStore.dg.getDefaultGraph
  }

  "adding a named graph should not pollute the default graph" in {
    store.addNamedGraph(makeUri("http://example.com/foo"), graph)

    defaultGraph must have size(0)
  }

}

class MemJenaGraphStore extends AbstractJenaGraphStoreTest(JenaStore(DatasetGraphFactory.createMem()))

class TDBJenaGraphStore extends AbstractJenaGraphStoreTest(JenaStore(TDBFactory.createDataset("test.tdb"))) {
  TDB.getContext().set(TDB.symUnionDefaultGraph, false)
}
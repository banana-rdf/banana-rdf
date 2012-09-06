package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import com.hp.hpl.jena.tdb.{ TDB, TDBFactory }
import JenaOperations._

abstract class JenaGraphStoreTest(jenaStore: JenaStore) extends GraphStoreTest[Jena](jenaStore) {

  def defaultGraph: Jena#Graph = jenaStore.readTransaction {
    BareJenaGraph(jenaStore.dg.getDefaultGraph)
  }

  "adding a named graph should not pollute the default graph" in {
    val s = jenaStore.execute {
      Command.append[Jena](makeUri("http://example.com/foo"), graphToIterable(graph))
    }
    s.getOrFail()
    defaultGraph.jenaGraph must have size(0)
  }

}

class JenaMemGraphStoreTest extends JenaGraphStoreTest(JenaStore(DatasetGraphFactory.createMem()))

class JenaTDBGraphStoreTest extends JenaGraphStoreTest(JenaStore(TDBFactory.createDataset("test.tdb"), defensiveCopy = true)) {
  TDB.getContext().set(TDB.symUnionDefaultGraph, false)
}

package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import com.hp.hpl.jena.tdb.{ TDB, TDBFactory }
import JenaOperations._
import concurrent.Future

abstract class JenaGraphStoreTest(jenaStore: JenaStore) extends GraphStoreTest[Jena](jenaStore) {

  def defaultGraph: Jena#Graph = jenaStore.readTransaction {
    BareJenaGraph(jenaStore.dg.getDefaultGraph)
  }

  "adding a named graph should not pollute the default graph" in {
    val s: Future[Unit] = jenaStore.execute {
      Command.append[Jena]( makeUri("http://example.com/foo"), graphToIterable(graph))
    }
    s.getOrFail()
    assert( defaultGraph.jenaGraph.size == 0)
  }

}

class JenaMemGraphStoreTest extends JenaGraphStoreTest(JenaStore(DatasetGraphFactory.createMem()))

class JenaTDBGraphStoreTest extends JenaGraphStoreTest(JenaStore(TDBFactory.createDataset("test.tdb"), defensiveCopy = true)) {
  TDB.getContext().set(TDB.symUnionDefaultGraph, false)
}

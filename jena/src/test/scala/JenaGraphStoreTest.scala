package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import com.hp.hpl.jena.tdb.{ TDB, TDBFactory }
import concurrent.Future

abstract class JenaGraphStoreTest(ops: RDFOps[Jena], jenaStore: JenaStore) extends GraphStoreTest[Jena](jenaStore) {

  import ops._

  def defaultGraph: Jena#Graph = jenaStore.readTransaction {
    jenaStore.dg.getDefaultGraph
  }

  "adding a named graph should not pollute the default graph" in {
    val s: Future[Unit] = jenaStore.execute {
      Command.append[Jena]( makeUri("http://example.com/foo"), graphToIterable(graph))
    }
    s.getOrFail()
    assert( defaultGraph.size == 0)
  }

}

class JenaMemGraphStoreTest extends JenaGraphStoreTest(Jena.Ops, JenaStore(DatasetGraphFactory.createMem())(Jena.Ops, Jena.JenaUtil))

class JenaTDBGraphStoreTest extends JenaGraphStoreTest(Jena.Ops, JenaStore(TDBFactory.createDataset("test.tdb"), defensiveCopy = true)(Jena.Ops, Jena.JenaUtil)) {
  TDB.getContext().set(TDB.symUnionDefaultGraph, false)
}

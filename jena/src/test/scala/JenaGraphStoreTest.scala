package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import JenaRDFReader._
import com.hp.hpl.jena.tdb.TDBFactory


abstract class AbstractJenaGraphStoreTest(val jenaStore: RDFStore[Jena, JenaSPARQL]) extends GraphStoreTest[Jena](jenaStore) {

  import JenaOperations._

  "adding a named graph should not pollute the default graph" in {
    store.addNamedGraph(makeUri("http://example.com/foo"), graph)
    val defaultGraph = store.getNamedGraph(null.asInstanceOf[Jena#URI])
    defaultGraph must have size(0)
  }

}

class MemJenaGraphStore extends AbstractJenaGraphStoreTest(JenaStore(DatasetGraphFactory.createMem()))

class TDBJenaGraphStore extends AbstractJenaGraphStoreTest(JenaStore(TDBFactory.createDataset("test.tdb")))


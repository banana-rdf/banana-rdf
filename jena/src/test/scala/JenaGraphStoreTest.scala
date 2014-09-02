package org.w3.banana.jena

import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.DatasetFactory
import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import com.hp.hpl.jena.tdb.{ TDB, TDBFactory }
import concurrent.Future

abstract class JenaGraphStoreTest(dataset: Dataset) extends GraphStoreTest[Jena, Dataset](dataset) {

  import ops._
  import graphStore.graphStoreSyntax._

  val defaultGraph: Jena#Graph = dataset.asDatasetGraph.getDefaultGraph

  "adding a named graph should not pollute the default graph" in {
    dataset.appendToGraph(makeUri("http://example.com/foo"), graph).getOrFail()
    assert(defaultGraph.size == 0)
  }

}

class JenaMemGraphStoreTest extends JenaGraphStoreTest({
  DatasetFactory.createMem()
})

class JenaTDBGraphStoreTest extends JenaGraphStoreTest({
  val dataset = TDBFactory.createDataset("test.tdb")
  dataset.getContext().set(TDB.symUnionDefaultGraph, false)
  dataset
})


package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._

class JenaGraphStoreTest() extends GraphStoreTest[Jena](
  JenaOperations,
  JenaDiesel,
  JenaGraphUnion,
  JenaStore(DatasetGraphFactory.createMem()),
  JenaRDFReader.RDFXMLReader,
  JenaGraphIsomorphism) {

  import JenaOperations._

  "adding a named graph should not pollute the default graph" in {
    store.addNamedGraph(URI("http://example.com/foo"), graph)
    val defaultGraph = store.getNamedGraph(null.asInstanceOf[Jena#URI])
    defaultGraph must have size(0)
  }

}

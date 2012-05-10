package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._

class JenaStoreTest() extends StoreTest[Jena](
  JenaOperations,
  JenaDiesel,
  JenaGraphUnion,
  JenaStore(DatasetGraphFactory.createMem()),
  JenaRDFXMLReader,
  JenaGraphIsomorphism) {

  import JenaOperations._

  "adding a named graph should not pollute the default graph" in {
    store.addNamedGraph(IRI("http://example.com/foo"), graph)
    val defaultGraph = store.getNamedGraph(null.asInstanceOf[Jena#IRI])
    defaultGraph must have size(0)
  }

}

package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._

class JenaAsyncStoreTest() extends AsyncStoreTest[Jena](
  JenaOperations,
  JenaDiesel,
  JenaGraphUnion,
  JenaStore(DatasetGraphFactory.createMem()),
  JenaRDFXMLReader,
  JenaGraphIsomorphism)

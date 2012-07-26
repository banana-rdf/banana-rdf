package org.w3.banana.jena

import org.w3.banana._
import Jena._
import com.hp.hpl.jena.sparql.core._

class JenaLinkedDataStoreTest extends LinkedDataStoreTest[Jena](JenaStore(DatasetGraphFactory.createMem()))

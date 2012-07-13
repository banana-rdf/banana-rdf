package org.w3.banana.jena

import org.w3.banana._
import Jena._
import com.hp.hpl.jena.sparql.core._

class JenaObjectStoreTest extends ObjectStoreTest[Jena](JenaStore(DatasetGraphFactory.createMem()))

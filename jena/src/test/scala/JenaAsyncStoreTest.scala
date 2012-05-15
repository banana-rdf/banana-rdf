package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import akka.actor.ActorSystem
import akka.util.Timeout

class JenaAsyncStoreTest() extends AsyncStoreTest[Jena](
  JenaOperations,
  JenaDiesel,
  JenaGraphUnion,
  {
    val store = JenaStore(DatasetGraphFactory.createMem())
    val system = ActorSystem("jena-asyncstore-test", AsyncRDFStore.DEFAULT_CONFIG)
    implicit val timeout = Timeout(1000)
    AsyncRDFStore(store, system)
  },
  JenaRDFXMLReader,
  JenaGraphIsomorphism)

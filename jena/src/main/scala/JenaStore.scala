package org.w3.banana.jena

import org.w3.banana._

case class JenaStore(store: Jena#Store)
extends RDFStore[Jena, JenaSPARQL]
with JenaGraphStore
with JenaSPARQLEngine {
  val ops = JenaSPARQLOperations
}

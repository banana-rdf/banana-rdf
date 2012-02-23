package org.w3.rdf.n3

import collection.immutable
import org.w3.rdf.RDFModule

trait ListenerAgent {

  def send(a: Any)

  def addPrefix(nameSpace: String, uri: String): Unit

  /* return the prefixMap, at this moment. useful for debugging. */
  def prefixes(): immutable.Map[String, String]
}
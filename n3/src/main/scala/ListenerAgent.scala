package org.w3.rdf.n3

import collection.immutable

trait ListenerAgent[T] {
  def send(a: T)

  def addPrefix(nameSpace: String, uri: String): Unit

  /* return the prefixMap, at this moment. useful for debugging. */
  def prefixes(): immutable.Map[String, String]
}
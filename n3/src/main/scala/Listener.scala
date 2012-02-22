package org.w3.rdf.n3

import collection.{immutable, mutable}

case class Listener(val queue: mutable.Queue[Any] = new mutable.Queue[Any]()) extends ListenerAgent[Any] {
  private val prefixs = new mutable.HashMap[String, String]

  def send(a: Any) = queue.enqueue(a)

  def addPrefix(nameSpace: String, uri: String) {
    prefixs.put(nameSpace,uri)
  }

  def prefixes = prefixs.toMap
}
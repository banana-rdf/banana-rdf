package org.w3.rdf.n3

import collection.mutable

case class Listener(val queue: mutable.Queue[Any] = new mutable.Queue[Any]()) extends ListenerAgent[Any] {
  def send(a: Any) = queue.enqueue(a)
}
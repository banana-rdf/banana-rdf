package org.w3.rdf.n3

trait ListenerAgent[T] {
  def send(a: T)
}
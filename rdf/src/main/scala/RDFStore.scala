package org.w3.banana

import scalaz.Free

trait RDFStore[Rdf <: RDF, M[_]] {
  def execute[A](script: Free[({ type l[+x] = Command[Rdf, x] })#l, A]): M[A]
  def shutdown(): Unit
}


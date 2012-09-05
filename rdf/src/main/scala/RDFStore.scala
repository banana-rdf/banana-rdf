package org.w3.banana

import scalaz.Free

trait RDFStore[Rdf <: RDF, M[_]] {
  def execute[A](script: Free[LDC[Rdf]#Command, A]): M[A]
}


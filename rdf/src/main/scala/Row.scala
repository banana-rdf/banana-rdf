package org.w3.banana

import scalaz.Validation

trait Row[Rdf <: RDF] {

  def apply(v: String): Validation[BananaException, Rdf#Node]

  def vars: Iterable[String]

}

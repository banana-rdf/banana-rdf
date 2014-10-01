package org.w3.banana.binder

import org.w3.banana._

trait ClassUrisFor[Rdf <: RDF, T] {
  def classes: Iterable[Rdf#URI]
}

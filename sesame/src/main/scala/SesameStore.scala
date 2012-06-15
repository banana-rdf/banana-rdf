package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.repository.sail.SailRepository


case class SesameStore(store: SailRepository)
extends RDFStore[Sesame, SesameSPARQL]
with SesameGraphStore
with SesameSPARQLEngine
with SPARQLEngineSyntax[Sesame, SesameSPARQL] {
  val ops = SesameSPARQLOperations
}


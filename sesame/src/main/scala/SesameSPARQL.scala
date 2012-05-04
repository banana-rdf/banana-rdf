package org.w3.rdf.sesame

import org.w3.rdf._

import org.openrdf.query._

trait SesameSPARQL extends SPARQL {

  type SelectQuery = TupleQuery

  type ConstructQuery = GraphQuery

  type AskQuery = BooleanQuery

  type Row = BindingSet

}

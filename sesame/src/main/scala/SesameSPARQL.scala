package org.w3.rdf.sesame

import org.w3.rdf._

import org.openrdf.query._

trait SesameSPARQL extends SPARQL {

  type SelectQuery = String // TupleQuery

  type ConstructQuery = String // GraphQuery

  type AskQuery = String // BooleanQuery

  type Row = BindingSet

}

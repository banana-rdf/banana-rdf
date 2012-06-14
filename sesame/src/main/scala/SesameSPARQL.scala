package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.query._

trait SesameSPARQL extends SPARQL {

  type Query = String

  type SelectQuery = String // TupleQuery

  type ConstructQuery = String // GraphQuery

  type AskQuery = String // BooleanQuery

  type Row = BindingSet

}

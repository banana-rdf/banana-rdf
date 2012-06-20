package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.query._
import org.openrdf.query.parser._
import info.aduna.iteration.CloseableIteration

trait SesameSPARQL extends SPARQL {

  type Query = ParsedQuery

  type SelectQuery = ParsedTupleQuery

  type ConstructQuery = ParsedGraphQuery

  type AskQuery = ParsedBooleanQuery

  type Solution = BindingSet

  // seriously?
  type Solutions = CloseableIteration[_ <: BindingSet, QueryEvaluationException]

}

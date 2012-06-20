package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.query._
import parser.{ParsedQuery, ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery}

trait SesameSPARQL extends SPARQL {

  type Query = ParsedQuery

  type SelectQuery = ParsedTupleQuery

  type ConstructQuery = ParsedGraphQuery

  type AskQuery = ParsedBooleanQuery

  type Solutions = info.aduna.iteration.CloseableIteration[_ <: org.openrdf.query.BindingSet, org.openrdf.query.QueryEvaluationException]

  type Solution = Binding
}


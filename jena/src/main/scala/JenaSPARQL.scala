package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.query.{Query => JenaQuery, ResultSet, QuerySolution}

/**
 * Use the underlying Jena Answers objects, without any wrapping.
 * This is useful when requiring Answers in the form provided
 * by the Jena Framework, usually because one wants to pass those
 * answers on to the Jena answer serialisers
 *
 */
trait JenaSPARQL extends SPARQL {

  type Query = JenaQuery

  type SelectQuery = JenaQuery

  type ConstructQuery = JenaQuery

  type AskQuery = JenaQuery

  type Solutions = ResultSet

  type Solution = QuerySolution

}

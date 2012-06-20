package org.w3.banana

trait SPARQL {

  type Query

  type SelectQuery <: Query

  type ConstructQuery  <: Query

  type AskQuery <: Query

  /**
   * A SPARQL Solution as defined
   * http://www.w3.org/TR/sparql11-query/#sparqlSolutions
   */
  type Solution

  /**
   * A SPARQL Solution Sequence as defined in
   * http://www.w3.org/TR/sparql11-query/#sparqlSolutions
   */
  type Solutions


}

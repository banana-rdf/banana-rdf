package org.w3.banana

trait SPARQL {

  type Query

  type SelectQuery <: Query

  type ConstructQuery  <: Query

  type AskQuery <: Query

  type Answers

  type Answer

}

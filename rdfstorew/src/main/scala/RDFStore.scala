package org.w3.banana.rdfstorew

import org.w3.banana._
import scala.scalajs.js


trait RDFStore extends RDF {
  // types related to the RDF datamodel
  type Graph = js.Dynamic
  type Triple = js.Dynamic
  type Node = js.Dynamic
  type URI = js.Dynamic
  type BNode = js.Dynamic
  type Literal = js.Dynamic
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = Nothing
  type NodeAny = Nothing
  type NodeConcrete = Nothing

  // types related to Sparql
  type Query = Nothing
  type SelectQuery = Nothing
  type ConstructQuery = Nothing
  type AskQuery = Nothing

  //FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
  type UpdateQuery = Nothing

  type Solution = Nothing
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Nothing
}

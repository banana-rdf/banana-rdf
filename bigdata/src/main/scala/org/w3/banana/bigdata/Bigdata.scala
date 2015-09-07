package org.w3.banana.bigdata


import com.bigdata.rdf.model._
import org.openrdf.query.BindingSet
import org.w3.banana.RDF

final case class BigdataMGraph(var graph: BigdataGraph)


/**
 * Anton: Bigdata local has higher priority for me that is why I start with it and call it just Bigdata
 */
trait Bigdata extends RDF{

  type Graph = BigdataGraph
  type Triple = BigdataStatement
  type Node = BigdataValue
  type URI = BigdataURI
  type BNode = BigdataBNode
  type Literal = BigdataLiteral
  type Lang = String

  type MGraph = BigdataMGraph

  // types for the graph traversal API
  type NodeMatch = BigdataValue
  type NodeAny = Null
  type NodeConcrete = BigdataValue

  // types related to Sparql
/*  type Query = BigdataSailQuery //It is a problem of bigdata that some BigdataParsed queries do not extend BigdataParsedQuery
  type SelectQuery = BigdataSailTupleQuery//BigdataParsedTupleQuery
  type ConstructQuery = BigdataSailGraphQuery
  type AskQuery = BigdataSailBooleanQuery*/

  type Query = String
  type SelectQuery = String
  type ConstructQuery = String
  type AskQuery = String

  //FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
  type UpdateQuery = String//BigdataSailUpdate

  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Vector[Solution]
}

object Bigdata extends BigdataModule
package org.w3.banana.bigdata

import com.bigdata.rdf.sail._
import org.openrdf.query.BindingSet
import org.w3.banana.RDF

/**
 * Anton: Bigdata local has higher priority for me that is why I start with it and call it just Bigdata
 */
trait Bigdata extends RDF with BigdataShared{


  // types related to Sparql
  type Query = BigdataSailQuery //It is a problem of bigdata that some BigdataParsed queries do not extend BigdataParsedQuery
  type SelectQuery = BigdataSailTupleQuery//BigdataParsedTupleQuery
  type ConstructQuery = BigdataSailGraphQuery
  type AskQuery = BigdataSailBooleanQuery

  //FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
  type UpdateQuery = BigdataSailUpdate

  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Vector[Solution]
}

object Bigdata extends BigdataModule
package org.w3.rdf.jena

import org.w3.rdf._

import com.hp.hpl.jena.query.{ Query, QuerySolution }

trait JenaSPARQL extends SPARQL {

  type SelectQuery = Query

  type ConstructQuery = Query

  type AskQuery = Query

  type Row = QuerySolution

}

package org.w3.banana.jena

import org.w3.banana._

import com.hp.hpl.jena.query.{ Query, QuerySolution }

trait JenaSPARQL extends SPARQL {

  type SelectQuery = Query

  type ConstructQuery = Query

  type AskQuery = Query

  type Row = QuerySolution

}

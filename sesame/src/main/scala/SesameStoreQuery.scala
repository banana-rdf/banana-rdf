package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.model.{ Graph => SesameGraph, BNode => SesameBNode }
import org.openrdf.repository._
import SesameUtil.{ withConnection, toIterable }
import org.openrdf.query.QueryLanguage

case class SesameStoreQuery(store: Repository) extends RDFQuery[Sesame, SesameSPARQL] {

  val TODO = "http://w3.org/TODO#"

  def executeSelectQuery(query: SesameSPARQL#SelectQuery): Iterable[SesameSPARQL#Row] = withConnection(store) { conn =>
    val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query, TODO)
    toIterable(tupleQuery.evaluate())
  }

  def getNode(row: SesameSPARQL#Row, v: String): Sesame#Node = row.getValue(v)

  def executeConstructQuery(query: SesameSPARQL#ConstructQuery): SesameGraph = withConnection(store) { conn =>
    val graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query, TODO)
    val triples = toIterable(graphQuery.evaluate())
    SesameOperations.Graph(triples)
  }
  
  def executeAskQuery(query: SesameSPARQL#AskQuery): Boolean =  withConnection(store) { conn =>
    val booleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query, TODO)
    booleanQuery.evaluate()
  }

}

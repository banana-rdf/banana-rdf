package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.query.parser.sparql.SPARQLParserFactory
import org.openrdf.query.parser.{ParsedBooleanQuery, ParsedGraphQuery, ParsedTupleQuery}

object SesameSPARQLOperations extends SPARQLOperations[Sesame, SesameSPARQL] {

  private val p = new SPARQLParserFactory().getParser()

  def getNode(row: SesameSPARQL#Row, v: String): Sesame#Node = row.getValue(v)

  def SelectQuery(query: String): SesameSPARQL#SelectQuery = Query(query).asInstanceOf[ParsedTupleQuery]
    
  def ConstructQuery(query: String): SesameSPARQL#ConstructQuery = Query(query).asInstanceOf[ParsedGraphQuery]

  def AskQuery(query: String): SesameSPARQL#AskQuery = Query(query).asInstanceOf[ParsedBooleanQuery]

  def Query(query: String): SesameSPARQL#Query = p.parseQuery(query,"http://todo.example/")

  def fold[T](query: SesameSPARQL#Query)(select: (SesameSPARQL#SelectQuery) => T,
                                         construct: (SesameSPARQL#ConstructQuery) => T,
                                         ask: SesameSPARQL#AskQuery => T) =
  query match {
    case qs: SesameSPARQL#SelectQuery => select(qs)
    case qc: SesameSPARQL#ConstructQuery => construct(qc)
    case qa: SesameSPARQL#AskQuery => ask(qa)
  }
}

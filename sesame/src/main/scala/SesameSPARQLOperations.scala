package org.w3.banana.sesame

import org.w3.banana._

object SesameSPARQLOperations extends SPARQLOperations[Sesame, SesameSPARQL] {

  def getNode(row: SesameSPARQL#Row, v: String): Sesame#Node = row.getValue(v)

  def SelectQuery(query: String): SesameSPARQL#SelectQuery = query
    
  def ConstructQuery(query: String): SesameSPARQL#ConstructQuery = query

  def AskQuery(query: String): SesameSPARQL#AskQuery = query

}

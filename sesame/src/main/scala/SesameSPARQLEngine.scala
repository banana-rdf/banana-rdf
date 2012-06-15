package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.model.{ Graph => SesameGraph, BNode => SesameBNode }
import org.openrdf.repository._
import sail.SailRepository
import SesameUtil.{ withConnection, toIterable }
import org.openrdf.query._
import scalaz.{Either3, Right3, Middle3, Left3}
import org.openrdf.sail.Sail
import org.openrdf.query.impl.EmptyBindingSet
import info.aduna.iteration.CloseableIteration

trait SesameSPARQLEngine extends SPARQLEngine[Sesame, SesameSPARQL] {

  def store: SailRepository

  val TODO = "http://w3.org/TODO#"
  val empty = new EmptyBindingSet()

  def connection = store.getConnection.getSailConnection

  def executeSelect(query: SesameSPARQL#SelectQuery): Iterable[SesameSPARQL#Row] = {
    //todo: one be able to specify binding sets. Jena also allows this
    val it: CloseableIteration[_ <: BindingSet, QueryEvaluationException] =
      connection.evaluate(query.getTupleExpr,null,empty,false)
    toIterable(it)
  }

  def executeConstruct(query: SesameSPARQL#ConstructQuery): SesameGraph = {
    val it: CloseableIteration[_ <: BindingSet, QueryEvaluationException] =
      connection.evaluate(query.getTupleExpr,null,empty,false)
    val sit = SesameUtil.toStatementIterable(it)
    SesameOperations.Graph(sit)
  }
  
  def executeAsk(query: SesameSPARQL#AskQuery): Boolean =  {
    val it: CloseableIteration[_ <: BindingSet, QueryEvaluationException] =
      connection.evaluate(query.getTupleExpr,null,empty,false)
    it.hasNext
  }


}

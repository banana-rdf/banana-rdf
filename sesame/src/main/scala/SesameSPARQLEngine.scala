package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.model.{ Graph => SesameGraph, BNode => SesameBNode }
import SesameUtil.{ withConnection, toIterable, toPartialFunction }
import org.openrdf.query.impl.EmptyBindingSet
import org.openrdf.repository.sail.SailRepository

trait SesameSPARQLEngine extends SPARQLEngine[Sesame, SesameSPARQL] {

  def store: SailRepository

  val TODO = "http://w3.org/TODO#"
  val empty = new EmptyBindingSet()

  def executeSelect(query: SesameSPARQL#SelectQuery): Iterable[PartialFunction[String, Sesame#Node]] = {
    //todo: one be able to specify binding sets. Jena also allows this
    withConnection(store){ conn =>
      val it = conn.evaluate(query.getTupleExpr,null,empty,false)
      toIterable(it) map toPartialFunction
    }
  }

  def executeConstruct(query: SesameSPARQL#ConstructQuery): SesameGraph =
    withConnection(store){ conn =>
      val it = conn.evaluate(query.getTupleExpr,null,empty,false)
      val sit = SesameUtil.toStatementIterable(it)
      SesameOperations.Graph(sit)
    }

  
  def executeAsk(query: SesameSPARQL#AskQuery): Boolean =
    withConnection(store) { conn =>
        conn.evaluate(query.getTupleExpr, null, empty, false).hasNext
    }


}

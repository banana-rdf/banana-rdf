package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import SesameUtil.withConnection
import org.openrdf.repository.sail.SailRepository
import scala.collection.JavaConversions._
import SesameUtil._
import info.aduna.iteration.CloseableIteration
import org.openrdf.sail.SailException
import org.openrdf.query.impl.EmptyBindingSet
import org.openrdf.query.parser.ParsedTupleQuery
import org.openrdf.query.TupleQueryResult

object SesameStore {

  def apply(store: SailRepository): RDFStore[Sesame, SesameSPARQL] =
    new SesameStore(store)

  def iter(st: CloseableIteration[_ <: Statement, SailException]) =
    new Iterator[Statement] {
      def hasNext: Boolean = st.hasNext
      def next(): Statement = st.next().asInstanceOf[Statement]
    }

}

class SesameStore(store: SailRepository) extends RDFStore[Sesame, SesameSPARQL] {

  def addNamedGraph(uri: Sesame#URI, graph: Sesame#Graph): Unit = {
    withConnection(store) { conn =>
      conn.removeStatements(null: Resource, null, null, uri)
      for (s: Statement <- graph.`match`(null, null, null)) {
        conn.addStatement(s.getSubject, s.getPredicate, s.getObject, uri)
      }
    }
  }

  def appendToNamedGraph(uri: Sesame#URI, graph: Sesame#Graph): Unit = {
    withConnection(store) { conn =>
      for (s: Statement <- graph.`match`(null, null, null))
        conn.addStatement(s.getSubject, s.getPredicate, s.getObject, uri)
    }
  }

  def getNamedGraph(uri: Sesame#URI): Sesame#Graph = {
    val graph = new GraphImpl
    withConnection(store) { conn =>
      for (s: Statement <- SesameStore.iter(conn.getStatements(null, null, null, false, uri)))
        graph.add(s)
    }
    graph
  }

  def removeGraph(uri: Sesame#URI): Unit = {
    withConnection(store) { conn =>
      conn.removeStatements(null: Resource, null, null, uri)
    }
  }

  val TODO = "http://w3.org/TODO#"

  val empty = new EmptyBindingSet()

  /**
   * Watch out connection is not closed here and neither is iterator.
   * (what does that mean? please help out)
   * @param query
   * @return
   */
  def executeSelect(query: SesameSPARQL#SelectQuery): SesameSPARQL#Solutions = {
    withConnection(store){ conn =>
      val res = conn.evaluate(query.getTupleExpr, null, empty, false)
      new TupleQueryResult {
        def hasNext = res.hasNext
        def next() = res.next
        def remove() { res.remove() }
        def getBindingNames = List[String]() //how do I get the bindings?
        def close() { res.close() }
      }
    }
  }

  def executeConstruct(query: SesameSPARQL#ConstructQuery): Sesame#Graph =
    withConnection(store){ conn =>
      val it = conn.evaluate(query.getTupleExpr, null, empty, false)
      val sit = SesameUtil.toStatementIterable(it)
      val res = SesameOperations.Graph(sit)
      it.close()
      res
    }
  
  def executeAsk(query: SesameSPARQL#AskQuery): Boolean =
    withConnection(store) { conn =>
      val it = conn.evaluate(query.getTupleExpr, null, empty, false)
      val res = it.hasNext
      it.close()
      res
    }

}


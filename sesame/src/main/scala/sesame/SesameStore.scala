package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import SesameUtil.withConnection
import org.openrdf.repository.sail.SailRepository
import scala.collection.JavaConversions._
import info.aduna.iteration.CloseableIteration
import org.openrdf.sail.SailException
import org.openrdf.query.impl.{MapBindingSet, EmptyBindingSet}
import org.openrdf.query.{BindingSet, TupleQueryResult}

object SesameStore {

  def apply(store: SailRepository): RDFStore[Sesame] =
    new SesameStore(store)

  def iter(st: CloseableIteration[_ <: Statement, SailException]) =
    new Iterator[Statement] {
      def hasNext: Boolean = st.hasNext
      def next(): Statement = st.next().asInstanceOf[Statement]
    }

}

class SesameStore(store: SailRepository) extends RDFStore[Sesame] {

  def appendToGraph(uri: Sesame#URI, graph: Sesame#Graph): Unit = {
    withConnection(store) { conn =>
      for (s: Statement <- graph.`match`(null, null, null))
        conn.addStatement(s.getSubject, s.getPredicate, s.getObject, uri)
    }
  }

  def patchGraph(uri: Sesame#URI, delete: Sesame#Graph, insert: Sesame#Graph): Unit = {
    withConnection(store) { conn =>
      for (s: Statement <- delete.`match`(null, null, null))
        conn.removeStatements(s.getSubject, s.getPredicate, s.getObject, uri)
      for (s: Statement <- insert.`match`(null, null, null))
        conn.addStatement(s.getSubject, s.getPredicate, s.getObject, uri)
    }
  }

  def getGraph(uri: Sesame#URI): Sesame#Graph = {
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
  def executeSelect(query: Sesame#SelectQuery, bindings: Map[String, Sesame#Node]): Sesame#Solutions = {
    withConnection(store) { conn =>
      val res = conn.evaluate(query.getTupleExpr, null, toSesame(bindings), false)

      new TupleQueryResult {
        def hasNext = res.hasNext
        def next() = res.next
        def remove() { res.remove() }
        lazy val getBindingNames = {
          val names = query.getTupleExpr.getBindingNames
          names.removeAll(bindings.keys)
          new java.util.ArrayList(names) //we don't get the order right here...
        }
        def close() { res.close() }
      }
    }
  }


  private def toSesame(bindings: Map[String, Sesame#Node]): BindingSet = {
    if (bindings.size == 0) empty
    else {
      val bndg = new MapBindingSet(bindings.size)
      for ((name, value) <- bindings) {
        bndg.addBinding(name, value)
      }
      bndg
    }
  }

  def executeConstruct(query: Sesame#ConstructQuery, bindings: Map[String, Sesame#Node]): Sesame#Graph =
    withConnection(store) { conn =>
      val it = conn.evaluate(query.getTupleExpr, null, toSesame(bindings), false)
      val sit = SesameUtil.toStatementIterable(it)
      val res = SesameOperations.makeGraph(sit)
      it.close()
      res
    }

  def executeAsk(query: Sesame#AskQuery, bindings: Map[String, Sesame#Node]): Boolean =
    withConnection(store) { conn =>
      val it = conn.evaluate(query.getTupleExpr, null, toSesame(bindings), false)
      val res = it.hasNext
      it.close()
      res
    }

}


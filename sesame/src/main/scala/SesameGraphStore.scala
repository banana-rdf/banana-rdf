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

trait SesameGraphStore extends GraphStore[Sesame] {

  def store: SailRepository

  def addNamedGraph(uri: Sesame#URI, graph: Sesame#Graph): Sesame#Store = {
    withConnection(store) { conn =>
      conn.removeStatements(null: Resource, null, null, uri)
      for (s: Statement<-graph.`match`(null,null,null)) {
          conn.addStatement(s.getSubject,s.getPredicate,s.getObject,uri)
      }
    }
    store
  }

  def appendToNamedGraph(uri: Sesame#URI, graph: Sesame#Graph): Sesame#Store = {
    withConnection(store) { conn =>
      for (s: Statement<-graph.`match`(null,null,null))
        conn.addStatement(s.getSubject,s.getPredicate,s.getObject,uri)
    }
    store
  }

  private def iter(st: CloseableIteration[_ <: Statement, SailException]) =
    new Iterator[Statement] {
      def hasNext: Boolean = st.hasNext
      def next(): Statement = st.next().asInstanceOf[Statement]
    }

  def getNamedGraph(uri: Sesame#URI): Sesame#Graph = {


    val graph = new GraphImpl
    withConnection(store) { conn =>
      for (s: Statement<-iter(conn.getStatements(null,null,null,false,uri)))
        graph.add(s)
    }
    graph
  }

  def removeGraph(uri: Sesame#URI): Sesame#Store = {
    withConnection(store) { conn =>
      conn.removeStatements(null: Resource, null, null, uri)
    }
    store
  }

}

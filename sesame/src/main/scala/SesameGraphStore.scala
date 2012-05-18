package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.repository._
import scala.collection.JavaConverters._
import org.openrdf.query._
import org.openrdf.rio.RDFHandler
import SesameUtil.withConnection

trait SesameGraphStore extends GraphStore[Sesame] {

  def store: Repository

  def addNamedGraph(uri: Sesame#IRI, graph: Sesame#Graph): Sesame#Store = {
    withConnection(store) { conn =>
      conn.remove(null: Resource, null, null, uri)
      conn.add(graph, uri)
    }
    store
  }

  def appendToNamedGraph(uri: Sesame#IRI, graph: Sesame#Graph): Sesame#Store = {
    withConnection(store) { conn =>
      conn.add(graph, uri)
    }
    store
  }

  class RDFCollector(graph: Sesame#Graph) extends RDFHandler {
    def startRDF(): Unit = () // println("startRDF")
    def endRDF(): Unit = () // println("endRDF")
    def handleComment(comment: String): Unit = () // println("comment: "+comment)
    def handleNamespace(prefix: String, uri: String): Unit = () // println("namespace")
    def handleStatement(statement: Statement): Unit = graph.add(statement)
  }

  def getNamedGraph(uri: Sesame#IRI): Sesame#Graph = {
    val graph = new GraphImpl
    withConnection(store) { conn =>
      conn.export(new RDFCollector(graph), uri)
    }
    graph
  }

  def removeGraph(uri: Sesame#IRI): Sesame#Store = {
    withConnection(store) { conn =>
      conn.remove(null: Resource, null, null, uri)
    }
    store
  }

}

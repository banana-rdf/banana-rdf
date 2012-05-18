package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic

trait JenaGraphStore extends GraphStore[Jena] {

  def store: DatasetGraph

  import JenaOperations._

  /* RDFStore */

  def addNamedGraph(uri: Jena#IRI, graph: Jena#Graph): Jena#Store = {
    store.removeGraph(uri)
    store.addGraph(uri, graph)
    store
  }

  def appendToNamedGraph(uri: Jena#IRI, graph: Jena#Graph): Jena#Store = {
    Graph.toIterable(graph) foreach { case Triple(s, p, o) =>
      store.add(uri, s, p, o)
    }
    store
  }

  def getNamedGraph(uri: Jena#IRI): Jena#Graph = {
    store.getGraph(uri)
  }

  def removeGraph(uri: Jena#IRI): Jena#Store = {
    store.removeGraph(uri)
    store
  }

}

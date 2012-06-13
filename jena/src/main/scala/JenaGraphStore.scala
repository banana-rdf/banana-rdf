package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic

trait JenaGraphStore extends GraphStore[Jena] {

  def store: DatasetGraph

  import JenaOperations._

  /* RDFStore */

  def addNamedGraph(uri: Jena#URI, graph: Jena#Graph): Jena#Store = {
//    store.removeGraph(uri)
    store.addGraph(uri, graph)
    store
  }

  def appendToNamedGraph(uri: Jena#URI, graph: Jena#Graph): Jena#Store = {
    Graph.toIterable(graph) foreach { case Triple(s, p, o) =>
      store.add(uri, s, p, o)
    }
    store
  }

  def getNamedGraph(uri: Jena#URI): Jena#Graph = {
    store.getGraph(uri)
  }

  def removeGraph(uri: Jena#URI): Jena#Store = {
    store.removeGraph(uri)
    store
  }

}

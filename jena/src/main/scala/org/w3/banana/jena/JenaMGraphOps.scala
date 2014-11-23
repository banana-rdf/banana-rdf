package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.Factory

trait JenaMGraphOps extends MGraphOps[Jena] {

  final def makeMGraph(graph: Jena#Graph): Jena#MGraph = graph

  final def makeEmptyMGraph(): Jena#MGraph = Factory.createDefaultGraph

  final def addTriple(graph: Jena#MGraph, triple: Jena#Triple): graph.type = {
    graph.add(triple)
    graph
  }

  final def removeTriple(graph: Jena#MGraph, triple: Jena#Triple): graph.type = {
    graph.delete(triple)
    graph
  }

  final def sizeMGraph(graph: Jena#MGraph): Int = graph.size

  final def makeIGraph(graph: Jena#MGraph): Jena#MGraph = graph

}

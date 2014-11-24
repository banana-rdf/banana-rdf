package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.Factory

trait JenaMGraphOps extends MGraphOps[Jena] {

  final def makeMGraph(graph: Jena#Graph): Jena#MGraph = graph

  final def makeEmptyMGraph(): Jena#MGraph = Factory.createDefaultGraph

  final def addTriple(mgraph: Jena#MGraph, triple: Jena#Triple): mgraph.type = {
    mgraph.add(triple)
    mgraph
  }

  final def removeTriple(mgraph: Jena#MGraph, triple: Jena#Triple): mgraph.type = {
    mgraph.delete(triple)
    mgraph
  }

  final def sizeMGraph(mgraph: Jena#MGraph): Int = mgraph.size

  final def makeIGraph(mgraph: Jena#MGraph): Jena#Graph = mgraph

}

package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model.impl.LinkedHashModel

trait SesameMGraphOps extends MGraphOps[Sesame] {

  final def makeMGraph(graph: Sesame#Graph): Sesame#MGraph = graph

  final def makeEmptyMGraph(): Sesame#MGraph = new LinkedHashModel

  final def addTriple(graph: Sesame#MGraph, triple: Sesame#Triple): graph.type = {
    graph.add(triple)
    graph
  }

  final def removeTriple(graph: Sesame#MGraph, triple: Sesame#Triple): graph.type = {
    graph.remove(triple)
    graph
  }

  final def sizeMGraph(graph: Sesame#MGraph): Int = graph.size

  final def makeIGraph(graph: Sesame#MGraph): Sesame#MGraph = graph

}

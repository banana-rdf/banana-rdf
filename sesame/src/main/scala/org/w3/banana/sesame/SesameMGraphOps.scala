package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model.impl.LinkedHashModel

trait SesameMGraphOps extends MGraphOps[Sesame] {

  final def makeMGraph(graph: Sesame#Graph): Sesame#MGraph = graph

  final def makeEmptyMGraph(): Sesame#MGraph = new LinkedHashModel

  final def addTriple(mgraph: Sesame#MGraph, triple: Sesame#Triple): mgraph.type = {
    mgraph.add(triple)
    mgraph
  }

  final def removeTriple(mgraph: Sesame#MGraph, triple: Sesame#Triple): mgraph.type = {
    mgraph.remove(triple)
    mgraph
  }

  final def sizeMGraph(mgraph: Sesame#MGraph): Int = mgraph.size

  final def makeIGraph(mgraph: Sesame#MGraph): Sesame#Graph = mgraph

}

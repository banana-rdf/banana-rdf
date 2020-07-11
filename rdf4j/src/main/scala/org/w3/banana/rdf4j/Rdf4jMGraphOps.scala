package org.w3.banana.rdf4j

import org.w3.banana._
import org.eclipse.rdf4j.model.impl.LinkedHashModel

trait Rdf4jMGraphOps extends MGraphOps[Rdf4j] { self: Rdf4jOps =>

  final def makeMGraph(graph: Rdf4j#Graph): Rdf4j#MGraph = {
    val mgraph = makeEmptyMGraph()
    addTriples(mgraph, self.getTriples(graph))
  }

  final def makeEmptyMGraph(): Rdf4j#MGraph = new LinkedHashModel

  final def addTriple(mgraph: Rdf4j#MGraph, triple: Rdf4j#Triple): mgraph.type = {
    mgraph.add(triple)
    mgraph
  }

  final def removeTriple(mgraph: Rdf4j#MGraph, triple: Rdf4j#Triple): mgraph.type = {
    mgraph.remove(triple)
    mgraph
  }

  final def exists(mgraph: Rdf4j#MGraph, triple: Rdf4j#Triple): Boolean =
    mgraph.contains(triple)

  final def sizeMGraph(mgraph: Rdf4j#MGraph): Int = mgraph.size

  final def makeIGraph(mgraph: Rdf4j#MGraph): Rdf4j#Graph = mgraph

}

package org.w3.banana

/** Operations over mutable graphs. */
trait MGraphOps[Rdf <: RDF] {

  def makeMGraph(graph: Rdf#Graph): Rdf#MGraph

  def makeEmptyMGraph(): Rdf#MGraph

  def addTriple(graph: Rdf#MGraph, triple: Rdf#Triple): graph.type

  def removeTriple(graph: Rdf#MGraph, triple: Rdf#Triple): graph.type

  def sizeMGraph(graph: Rdf#MGraph): Int

  def makeIGraph(graph: Rdf#MGraph): Rdf#MGraph

}

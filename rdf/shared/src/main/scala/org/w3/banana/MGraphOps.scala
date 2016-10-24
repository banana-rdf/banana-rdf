package org.w3.banana

/** Operations over mutable graphs. */
trait MGraphOps[Rdf <: RDF] {

  def makeMGraph(graph: Rdf#Graph): Rdf#MGraph

  def makeEmptyMGraph(): Rdf#MGraph

  def exists(mgraph: Rdf#MGraph, triple: Rdf#Triple): Boolean

  def addTriple(mgraph: Rdf#MGraph, triple: Rdf#Triple): mgraph.type

  def removeTriple(mgraph: Rdf#MGraph, triple: Rdf#Triple): mgraph.type

  def sizeMGraph(mgraph: Rdf#MGraph): Int

  def makeIGraph(mgraph: Rdf#MGraph): Rdf#Graph

  final def addTriples(mgraph: Rdf#MGraph, triples: TraversableOnce[Rdf#Triple]): mgraph.type = {
    triples.foreach(triple => addTriple(mgraph, triple))
    mgraph
  }

  final def removeTriples(mgraph: Rdf#MGraph, triples: TraversableOnce[Rdf#Triple]): mgraph.type = {
    triples.foreach(triple => removeTriple(mgraph, triple))
    mgraph
  }

}

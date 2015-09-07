package org.w3.banana.bigdata

import org.openrdf.model.impl.LinkedHashModel
import org.w3.banana.MGraphOps

trait BigdataMGraphOps extends MGraphOps[Bigdata] {
  self: BigdataOps =>


  def makeMGraph(graph: Bigdata#Graph): Bigdata#MGraph = {
    val mgraph = makeEmptyMGraph()
    addTriples(mgraph, self.getTriples(graph))
  }

  def makeEmptyMGraph(): Bigdata#MGraph = BigdataMGraph(self.emptyGraph)

  //def addTriple(mgraph: Bigdata#MGraph, triple: Bigdata#Triple): Bigdata#MGraph = mgraph + triple

  //def removeTriple(mgraph: Bigdata#MGraph, triple: Bigdata#Triple): Bigdata#MGraph = mgraph - triple

  override def addTriple(mgraph: Bigdata#MGraph, triple: Bigdata#Triple): mgraph.type = {
    mgraph.graph = mgraph.graph + triple
    mgraph
  }

  override def removeTriple(mgraph: Bigdata#MGraph, triple: Bigdata#Triple): mgraph.type = {
    mgraph.graph = mgraph.graph - triple
    mgraph
  }

  final def exists(mgraph: Bigdata#MGraph, triple: Bigdata#Triple): Boolean = mgraph.graph.contains(triple)

  final def sizeMGraph(mgraph: Bigdata#MGraph): Int = mgraph.size

  final def makeIGraph(mgraph: Bigdata#MGraph): Bigdata#Graph = mgraph.graph
}

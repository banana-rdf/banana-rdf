package org.w3.banana
package n3js

import plantain.{ model => pm }

trait N3jsMGraphOps extends MGraphOps[N3js] {

  def makeMGraph(graph: N3js#Graph): N3js#MGraph = new model.MGraph(graph)

  def makeEmptyMGraph(): N3js#MGraph = new model.MGraph(pm.Graph.empty)

  def addTriple(mgraph: N3js#MGraph, triple: N3js#Triple): mgraph.type = {
    val (s, p, o) = triple
    mgraph.graph += (s, p, o)
    mgraph
  }

  def removeTriple(mgraph: N3js#MGraph, triple: N3js#Triple): mgraph.type = {
    val (s, p, o) = triple
    mgraph.graph -= (s, p, o)
    mgraph
  }

  def exists(mgraph: N3js#MGraph, triple: N3js#Triple): Boolean = {
    val (s, p, o) = triple
    mgraph.graph.find(Some(s), Some(p), Some(o)).nonEmpty
  }

  def sizeMGraph(mgraph: N3js#MGraph): Int = mgraph.graph.size

  def makeIGraph(mgraph: N3js#MGraph): N3js#Graph = mgraph.graph

}

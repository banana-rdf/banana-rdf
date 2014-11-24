package org.w3.banana.plantain

import org.w3.banana._

trait PlantainMGraphOps extends MGraphOps[Plantain] {

  def makeMGraph(graph: Plantain#Graph): Plantain#MGraph = new model.MGraph(graph)

  def makeEmptyMGraph(): Plantain#MGraph = new model.MGraph(model.Graph.empty)

  def addTriple(mgraph: Plantain#MGraph, triple: Plantain#Triple): mgraph.type = {
    val (s, p, o) = triple
    mgraph.graph += (s, p, o)
    mgraph
  }

  def removeTriple(mgraph: Plantain#MGraph, triple: Plantain#Triple): mgraph.type = {
    val (s, p, o) = triple
    mgraph.graph -= (s, p, o)
    mgraph
  }

  def sizeMGraph(mgraph: Plantain#MGraph): Int = mgraph.graph.size

  def makeIGraph(mgraph: Plantain#MGraph): Plantain#Graph = mgraph.graph

}

package org.w3.banana.plantain

import org.w3.banana._

trait PlantainMGraphOps extends MGraphOps[Plantain] {

  def makeMGraph(graph: Plantain#Graph): Plantain#MGraph = new model.MGraph(graph)

  def makeEmptyMGraph(): Plantain#MGraph = new model.MGraph(model.IntHexastoreGraph.empty)

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

  def exists(mgraph: Plantain#MGraph, triple: Plantain#Triple): Boolean = {
    val (s, p, o) = triple
    mgraph.graph.find(Some(s), Some(p), Some(o)).nonEmpty
  }

  def sizeMGraph(mgraph: Plantain#MGraph): Int = mgraph.graph.size

  def makeIGraph(mgraph: Plantain#MGraph): Plantain#Graph = mgraph.graph

}

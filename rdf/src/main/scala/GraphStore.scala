package org.w3.banana

/**
 * to manipulate named graphs
 */
trait GraphStore[Rdf <: RDF] {

  def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Unit

  def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Unit

  def getNamedGraph(uri: Rdf#URI): Rdf#Graph

  def removeGraph(uri: Rdf#URI): Unit

}

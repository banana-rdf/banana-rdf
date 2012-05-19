package org.w3.banana

/**
 * to manipulate named graphs
 */
trait GraphStore[Rdf <: RDF] {

  def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Rdf#Store

  def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Rdf#Store

  def getNamedGraph(uri: Rdf#URI): Rdf#Graph

  def removeGraph(uri: Rdf#URI): Rdf#Store

}

package org.w3.banana
/**
 * to manipulate named graphs
 */
trait MGraphStore[Rdf <: RDF, M[_]] {

  def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): M[Unit]

  def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): M[Unit]

  def getNamedGraph(uri: Rdf#URI): M[Rdf#Graph]

  def removeGraph(uri: Rdf#URI): M[Unit]

}

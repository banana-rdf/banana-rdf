package org.w3.banana

import org.w3.banana.RDF
//
//
//trait RDFOps2[T <: RDFObj](using val rdf: T) {
//  def emptyGraph: rdf.Graph
//  def fromUri(uri: rdf.URI): String
//  def makeUri(s: String): rdf.URI
//}
//
//trait PointedGraph2[Rdf <: RDFObj] {
//  def pointer: Rdf#Node
//  def graph: Rdf#Graph
//}

final case class PG[Rdf <: RDF](uri: RDF.Node[Rdf], graph: RDF.Graph[Rdf])

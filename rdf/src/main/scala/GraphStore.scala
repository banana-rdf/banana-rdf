package org.w3.banana

import scalaz._
import Id._
import util._

trait GraphStore[Rdf <: RDF] extends MGraphStore[Rdf, Id]

trait AsyncGraphStore[Rdf <: RDF] extends MGraphStore[Rdf, BananaFuture]

/**
 * to manipulate named graphs
 */
trait MGraphStore[Rdf <: RDF, M[_]] {

  def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): M[Unit]

  def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): M[Unit]

  def getGraph(uri: Rdf#URI): M[Rdf#Graph]

  def removeGraph(uri: Rdf#URI): M[Unit]

}


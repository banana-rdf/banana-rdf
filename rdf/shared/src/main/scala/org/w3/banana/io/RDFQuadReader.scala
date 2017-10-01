package org.w3.banana.io

import java.io.{InputStream, Reader}

import org.w3.banana.RDF

trait RDFQuadReader[Rdf <: RDF, M[_], +S] extends RDFReader[Rdf, M, S] {

  def readAll(is: InputStream, base: String): M[Map[Option[Rdf#Node], Rdf#Graph]]

  def readAll(in: Reader, base: String): M[Map[Option[Rdf#Node], Rdf#Graph]]

  def read(is: InputStream, base: String, graphName: Rdf#URI): M[Rdf#Graph]

  def read(in: Reader, base: String, graphName: Rdf#URI): M[Rdf#Graph]

  def readDefaultGraph(is: InputStream, base: String): M[Rdf#Graph]

  def readDefaultGraph(in: Reader, base: String): M[Rdf#Graph]

}

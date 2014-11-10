package org.w3.banana
package io

import java.io.OutputStream

trait RDFWriter[Rdf <: RDF, M[_], +T] extends Writer[Rdf#Graph,M,T] {


  def write(graph: Rdf#Graph, os: OutputStream, base: String): M[Unit]

  def asString(graph: Rdf#Graph, base: String): M[String]

}


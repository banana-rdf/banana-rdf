package org.w3.banana
package io

import java.io.OutputStream

trait RDFWriter[Rdf <: RDF, M[_], +T] extends Writer[Rdf#Graph,M,T] {

  override def write(obj: Rdf#Graph, os: OutputStream, base: String): M[Unit] = write(obj, os, base, Set())

  def write(graph: Rdf#Graph, os: OutputStream, base: String, prefixes: Set[Prefix[Rdf]]): M[Unit]

  override def asString(obj: Rdf#Graph, base: String): M[String] = asString(obj, base, Set())

  def asString(graph: Rdf#Graph, base: String, prefixes: Set[Prefix[Rdf]]): M[String]

}

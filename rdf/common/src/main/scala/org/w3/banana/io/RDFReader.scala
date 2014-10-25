package org.w3.banana
package io

import java.io._
import scala.util.Try

/** RDF readers for a given syntax. */
trait RDFReader[Rdf <: RDF, M[_], +S] {

  /** Tries parsing an RDF Graph from an [[InputStream]] and a base URI.
    * 
    * If the encoding for the input is known, prefer the [[Reader]]
    * version of this function.
    */
  def read(is: InputStream, base: String): M[Rdf#Graph]

  /** Tries parsing an RDF Graph from a [[Reader]] and a base URI. */
  def read(reader: Reader, base: String): M[Rdf#Graph]
}

package org.w3.banana
package io

import java.io._

/** RDF readers.
  * 
  * All functions return results in the context `M`. `S` is a phantom
  * type for the syntax.
  */
trait RDFReader[Rdf <: RDF, M[_], +S] {

  /** Tries parsing an RDF Graph from an [[java.io.InputStream]] and a
    * base URI.
    */
  def read(is: InputStream, base: String): M[Rdf#Graph]

  /** Tries parsing an RDF Graph from a [[java.io.Reader]] and a base URI.
    * @param base the base URI to use, to resolve relative URLs found in the InputStream
    **/
  def read(reader: Reader, base: String): M[Rdf#Graph]
}

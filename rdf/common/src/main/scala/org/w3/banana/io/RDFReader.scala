package org.w3.banana
package io

import java.io._

/** RDF readers for a given syntax. */
trait RDFReader[Rdf <: RDF, M[_], +S] {

  def syntax: Syntax[S]


  /** Tries parsing an RDF Graph from an [[java.io.InputStream]] and a base URI.
    * 
    * If the encoding for the input is known, prefer the Reader
    * version of this function.
    * @param base the base URI to use, to resolve relative URLs found in the InputStream
    */
  def read(is: InputStream, base: String): M[Rdf#Graph]

  /** Tries parsing an RDF Graph from a [[java.io.Reader]] and a base URI.
    * @param base the base URI to use, to resolve relative URLs found in the InputStream
    **/
  def read(reader: Reader, base: String): M[Rdf#Graph]
}

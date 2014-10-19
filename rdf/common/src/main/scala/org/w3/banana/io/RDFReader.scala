package org.w3.banana
package io

import java.io._

import scala.util._

/**
 * Blocking Reader for an implementeation of RDF and a Syntax S
 */
trait RDFReader[Rdf <: RDF, +S] {

  /**
   * parse from the Input Stream and have the parser guess the encoding. If no encoding guessing
   * is needed use the reader method that takes a Reader.  This guessing may be more or less successful.
   * @param is InputStream
   * @param base Url to use to resolve relative URLs  ( as String ) //todo: why not as a RDF#URI ?
   * @return A Success[Graph] or a Failure
   * //todo: it may be more appropriate to have an encoding guessing function
   */
  def read(is: InputStream, base: String): Try[Rdf#Graph]

  /**
   * Parse from the Reader. Readers have already made the encoding decision, so there is no decision left
   * here to make
   * @param reader
   * @param base URI for all relative URIs in reader //todo: should be a URI
   * @return Success of a Graph or Failure
   */
  def read(reader: Reader, base: String): Try[Rdf#Graph]
}

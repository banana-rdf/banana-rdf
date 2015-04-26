package org.w3.banana
package io

/** RDF loader. */
trait RDFLoader[Rdf <: RDF, M[_]] {

  /** Read triples from the given location.
   *
   * The syntax is determined from input source URI
   * (content negotiation or extension). */
   def load(url: java.net.URL) : M[Rdf#Graph]
}

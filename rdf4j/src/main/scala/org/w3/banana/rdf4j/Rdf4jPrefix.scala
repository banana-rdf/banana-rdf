package org.w3.banana.rdf4j

import org.w3.banana._

class Rdf4jPrefix(implicit ops: RDFOps[Rdf4j]) {

  val rdf = RDFPrefix[Rdf4j]

  val xsd = XSDPrefix[Rdf4j]

  val dc = DCPrefix[Rdf4j]

  val foaf = FOAFPrefix[Rdf4j]

}

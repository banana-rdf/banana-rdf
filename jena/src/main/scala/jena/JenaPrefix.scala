package org.w3.banana.jena

import org.w3.banana._

class JenaPrefix(ops: RDFOps[Jena]) {

  val rdf = RDFPrefix[Jena]

  val xsd = XSDPrefix[Jena]

  val dc = DCPrefix[Jena]

  val foaf = FOAFPrefix[Jena]

}

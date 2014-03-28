package org.w3.banana.jena

import org.w3.banana._

class JenaPrefix(ops: RDFOps[Jena]) {

  val rdf = RDFPrefix(ops)

  val xsd = XSDPrefix(ops)

  val dc = DCPrefix(ops)

  val foaf = FOAFPrefix(ops)

}

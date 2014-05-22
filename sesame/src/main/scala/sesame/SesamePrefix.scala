package org.w3.banana.sesame

import org.w3.banana._

class SesamePrefix(implicit Ops: RDFOps[Sesame]) {

  val rdf = RDFPrefix(Ops)

  val xsd = XSDPrefix(Ops)

  val dc = DCPrefix(Ops)

  val foaf = FOAFPrefix(Ops)

}

package org.w3.banana.sesame

import org.w3.banana._

class SesamePrefix(implicit ops: RDFOps[Sesame]) {

  val rdf = RDFPrefix[Sesame]

  val xsd = XSDPrefix[Sesame]

  val dc = DCPrefix[Sesame]

  val foaf = FOAFPrefix[Sesame]

}

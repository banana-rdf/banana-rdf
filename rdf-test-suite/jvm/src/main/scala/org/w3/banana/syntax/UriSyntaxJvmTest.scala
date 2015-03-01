package org.w3.banana.syntax

import org.w3.banana._
import java.net.URL
import zcheck.SpecLite

class UriSyntaxJvmTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

  import ops._

  "transforming java URLs to Rdf#URI" in {
    val card = "http://bblfish.net/people/henry/card"
    val uri: Rdf#URI = URI(card)

    new URL(card).toUri must_==(uri)
  }

}

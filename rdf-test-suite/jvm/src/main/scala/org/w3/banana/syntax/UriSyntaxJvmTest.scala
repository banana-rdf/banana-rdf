package org.w3.banana.syntax

import org.w3.banana._
import java.net.URL

import org.scalatest.{Matchers, WordSpec}


class UriSyntaxJvmTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  "transforming java URLs to Rdf#URI" in {
    val card = "http://bblfish.net/people/henry/card"
    val uri: Rdf#URI = URI(card)

    new URL(card).toUri shouldEqual uri
  }

}

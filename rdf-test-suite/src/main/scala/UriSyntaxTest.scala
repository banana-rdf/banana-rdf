package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers

abstract class UriSyntaxTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) extends WordSpec with MustMatchers {

  import diesel._

  "URI.fragmentLess should remove the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragmentLess must be (URI("http://example.com/foo"))
  }

  "URI.fragment should set the fragment part of a URI" in {
    val uri = URI("http://example.com/foo")
    uri.fragment("bar") must be (URI("http://example.com/foo#bar"))
  }

  "URI.fragment should return the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragment must be (Some("bar"))

    val uriNoFrag = URI("http://example.com/foo")
    uriNoFrag.fragment must be (None)
  }  

}

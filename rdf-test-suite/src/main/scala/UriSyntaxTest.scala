package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers

abstract class UriSyntaxTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf]) extends WordSpec with MustMatchers {

  import diesel._

  ".fragmentLess should remove the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragmentLess must be(URI("http://example.com/foo"))
  }

  ".fragment should set the fragment part of a URI" in {
    val uri = URI("http://example.com/foo")
    uri.fragment("bar") must be(URI("http://example.com/foo#bar"))
  }

  ".fragment should return the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragment must be(Some("bar"))
    val uriNoFrag = URI("http://example.com/foo")
    uriNoFrag.fragment must be(None)
  }

  "/ should create a sub-resource uri" in {
    (uri("http://example.com/foo") / "bar") must be(uri("http://example.com/foo/bar"))
    (uri("http://example.com/foo/") / "bar") must be(uri("http://example.com/foo/bar"))
  }

  "resolve should resolve the uri against the passed string" in {
    uri("http://example.com/foo").resolve("bar") must be(uri("http://example.com/bar"))
    uri("http://example.com/foo/").resolve("bar") must be(uri("http://example.com/foo/bar"))
  }

  "resolveAgainst should work like resolve, just the other way around" in {
    uri("http://example.com/foo").resolveAgainst(uri("whatever-not-absolute")) must be(URI("http://example.com/foo"))
    uri("bar").resolveAgainst(uri("http://example.com/foo")) must be(URI("http://example.com/bar"))
    uri("#bar").resolveAgainst(uri("http://example.com/foo")) must be(URI("http://example.com/foo#bar"))
    uri("#bar").resolveAgainst(uri("http://example.com/foo/")) must be(URI("http://example.com/foo/#bar"))
    uri("bar").resolveAgainst(uri("http://example.com/foo")) must be(URI("http://example.com/foo#bar"))
  }

}

package org.w3.banana.syntax

import org.w3.banana._
import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers.MustMatchers

abstract class UriSyntaxTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with MustMatchers {

  import ops._

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
    (URI("http://example.com/foo") / "bar") must be(URI("http://example.com/foo/bar"))
    (URI("http://example.com/foo/") / "bar") must be(URI("http://example.com/foo/bar"))
  }

  "resolve should resolve the uri against the passed string" in {
    URI("http://example.com/foo").resolve("bar") must be(URI("http://example.com/bar"))
    URI("http://example.com/foo/").resolve("bar") must be(URI("http://example.com/foo/bar"))
  }

  "resolveAgainst should work like resolve, just the other way around" in {
    URI("http://example.com/foo").resolveAgainst(URI("#bar")) must be(URI("http://example.com/foo"))
    /* URI("bar").resolveAgainst(URI("http://example.com/foo")) must be(URI("http://example.com/bar")) */
    URI("#bar").resolveAgainst(URI("http://example.com/foo")) must be(URI("http://example.com/foo#bar"))
    URI("#bar").resolveAgainst(URI("http://example.com/foo/")) must be(URI("http://example.com/foo/#bar"))
    /* URI("bar").resolveAgainst(URI("http://example.com/foo")) must be(URI("http://example.com/bar")) */
  }

  "should be able to create and work with relative URIs" taggedAs(SesameWIP) in {
    val me = URI("/people/card/henry#me")
    me.fragment must be(Some("me"))
    me.fragmentLess must be(URI("/people/card/henry"))
    val host = URI("http://bblfish.net")
    me.resolveAgainst(host) must be(URI("http://bblfish.net/people/card/henry#me"))
  }

}

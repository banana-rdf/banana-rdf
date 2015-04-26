package org.w3.banana.syntax

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana._


class UriSyntaxTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  ".fragmentLess should remove the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragmentLess shouldEqual URI("http://example.com/foo")
  }

  ".fragment should set the fragment part of a URI" in {
    val uri = URI("http://example.com/foo")
    uri.withFragment("bar") shouldEqual URI("http://example.com/foo#bar")
  }

  ".fragment should return the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragment shouldEqual Some("bar")
    val uriNoFrag = URI("http://example.com/foo")
    uriNoFrag.fragment shouldEqual None
  }

  "isPureFragment should should say if a URI is a pure fragment" in {
    (! URI("http://example.com/foo").isPureFragment)  shouldEqual true
    (! URI("http://example.com/foo#bar").isPureFragment) shouldEqual true
    URI("#bar").isPureFragment shouldEqual true
  }

  "/ should create a sub-resource uri" in {
    (URI("http://example.com/foo") / "bar") shouldEqual URI("http://example.com/foo/bar")
    (URI("http://example.com/foo/") / "bar") shouldEqual URI("http://example.com/foo/bar")
  }

  "resolve should resolve the uri against the passed string" in {
    URI("http://example.com/foo").resolve(URI("bar")) shouldEqual URI("http://example.com/bar")
    URI("http://example.com/foo").resolve(URI(".")) shouldEqual URI("http://example.com/")
    URI("http://example.com/foo/").resolve(URI("bar")) shouldEqual URI("http://example.com/foo/bar")
    URI("http://example.com/foo/").resolve(URI(".")) shouldEqual URI("http://example.com/foo/")
  }

  "resolveAgainst should work like resolve, just the other way around" in {
    // the following test does not make sense as the resolution base Uri must be absolute
    // URI("http://example.com/foo").resolveAgainst(URI("#bar")) shouldEqual (URI("http://example.com/foo"))
    URI("bar").resolveAgainst(URI("http://example.com/foo")) shouldEqual URI("http://example.com/bar")
    URI("#bar").resolveAgainst(URI("http://example.com/foo")) shouldEqual URI("http://example.com/foo#bar")
    URI("#bar").resolveAgainst(URI("http://example.com/foo/")) shouldEqual URI("http://example.com/foo/#bar")
    URI("bar").resolveAgainst(URI("http://example.com/foo")) shouldEqual URI("http://example.com/bar")
    (URI("bar"): Rdf#Node).resolveAgainst(URI("http://example.com/foo")) shouldEqual URI("http://example.com/bar")
  }

  ".relativize() should relativize the uri against the passed string" in {
    URI("http://example.com/foo").relativize(URI("http://example.com/foo#bar")) shouldEqual URI("#bar")
    (URI("http://example.com/foo"): Rdf#Node).relativize(URI("http://example.com/foo#bar")) shouldEqual URI("#bar")
    URI("http://example.com/foo#bar").relativizeAgainst(URI("http://example.com/foo")) shouldEqual URI("#bar")

    URI("http://example.com/ldpc").relativize(URI("http://example.com/ldpc/")) shouldEqual URI("")
    URI("http://example.com/ldpc").relativize(URI("http://example.com/ldpc")) shouldEqual URI("")
    URI("http://example.com/ldpc").relativize(URI("http://example.com/ldpc/entry")) shouldEqual URI("entry")

  }

  "should be able to create and work with relative URIs" in {
    val me = URI("/people/card/henry#me")
    me.fragment shouldEqual Some("me")
    me.fragmentLess shouldEqual URI("/people/card/henry")
    val host = URI("http://bblfish.net")
    me.resolveAgainst(host) shouldEqual URI("http://bblfish.net/people/card/henry#me")
  }

  "transforming java URIs to Rdf#URI" in {
    val card = "http://bblfish.net/people/henry/card"
    val uri: Rdf#URI = URI(card)

    new java.net.URI(card).toUri shouldEqual uri
  }

}

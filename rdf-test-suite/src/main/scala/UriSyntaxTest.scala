package org.w3.banana.syntax

import java.net.URL

import org.scalatest._
import org.w3.banana._

abstract class UriSyntaxTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  ".fragmentLess should remove the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragmentLess should be(URI("http://example.com/foo"))
  }

  ".fragment should set the fragment part of a URI" in {
    val uri = URI("http://example.com/foo")
    uri.withFragment("bar") should be(URI("http://example.com/foo#bar"))
  }

  ".fragment should return the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragment should be(Some("bar"))
    val uriNoFrag = URI("http://example.com/foo")
    uriNoFrag.fragment should be(None)
  }

  "isPureFragment should should say if a URI is a pure fragment" in {
    URI("http://example.com/foo").isPureFragment should be(false)
    URI("http://example.com/foo#bar").isPureFragment should be(false)
    URI("#bar").isPureFragment should be(true)
  }

  "/ should create a sub-resource uri" in {
    (URI("http://example.com/foo") / "bar") should be(URI("http://example.com/foo/bar"))
    (URI("http://example.com/foo/") / "bar") should be(URI("http://example.com/foo/bar"))
  }

  "resolve should resolve the uri against the passed string" in {
    URI("http://example.com/foo").resolve(URI("bar")) should be(URI("http://example.com/bar"))
    URI("http://example.com/foo").resolve(URI(".")) should be(URI("http://example.com/"))
    URI("http://example.com/foo/").resolve(URI("bar")) should be(URI("http://example.com/foo/bar"))
    URI("http://example.com/foo/").resolve(URI(".")) should be(URI("http://example.com/foo/"))
  }

  "resolveAgainst should work like resolve, just the other way around" in {
    // the following test does not make sense as the resolution base Uri must be absolute
    // URI("http://example.com/foo").resolveAgainst(URI("#bar")) should be(URI("http://example.com/foo"))
    URI("bar").resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/bar"))
    URI("#bar").resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/foo#bar"))
    URI("#bar").resolveAgainst(URI("http://example.com/foo/")) should be(URI("http://example.com/foo/#bar"))
    URI("bar").resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/bar"))
    (URI("bar"): Rdf#Node).resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/bar"))
  }

  ".relativize() should relativize the uri against the passed string" in {
    URI("http://example.com/foo").relativize(URI("http://example.com/foo#bar")) should be(URI("#bar"))
    (URI("http://example.com/foo"): Rdf#Node).relativize(URI("http://example.com/foo#bar")) should be(URI("#bar"))
    URI("http://example.com/foo#bar").relativizeAgainst(URI("http://example.com/foo")) should be(URI("#bar"))
  }

  "should be able to create and work with relative URIs" in {
    val me = URI("/people/card/henry#me")
    me.fragment should be(Some("me"))
    me.fragmentLess should be(URI("/people/card/henry"))
    val host = URI("http://bblfish.net")
    me.resolveAgainst(host) should be(URI("http://bblfish.net/people/card/henry#me"))
  }

  "transforming java URIs and URLs to Rdf#URI" in {
    val card = "http://bblfish.net/people/henry/card"
    val uri: Rdf#URI = URI(card)

    new URL(card).toUri should be(uri)
    new java.net.URI(card).toUri should be(uri)
  }

}

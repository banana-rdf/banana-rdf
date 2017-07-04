package org.w3.banana.syntax

import org.w3.banana._
import java.net.URL

import org.scalatest.WordSpec

class UriSyntaxTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec {

  import ops._

  ".fragmentLess should remove the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    assert(uri.fragmentLess === (URI("http://example.com/foo")))
  }

  ".fragment should set the fragment part of a URI" in {
    val uri = URI("http://example.com/foo")
    assert(uri.withFragment("bar") === (URI("http://example.com/foo#bar")))
  }

  ".fragment should return the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    assert(uri.fragment === (Some("bar")))
    val uriNoFrag = URI("http://example.com/foo")
    assert(uriNoFrag.fragment === (None))
  }

  "isPureFragment should should say if a URI is a pure fragment" in {
    assert(! URI("http://example.com/foo").isPureFragment)
    assert(! URI("http://example.com/foo#bar").isPureFragment)
    assert(URI("#bar").isPureFragment)
  }

  "/ should create a sub-resource uri" in {
    assert((URI("http://example.com/foo") / "bar") === (URI("http://example.com/foo/bar")))
    assert((URI("http://example.com/foo/") / "bar") === (URI("http://example.com/foo/bar")))
  }

  "resolve should resolve the uri against the passed string" in {
    assert(URI("http://example.com/foo").resolve(URI("bar")) === (URI("http://example.com/bar")))
    assert(URI("http://example.com/foo").resolve(URI(".")) === (URI("http://example.com/")))
    assert(URI("http://example.com/foo/").resolve(URI("bar")) === (URI("http://example.com/foo/bar")))
    assert(URI("http://example.com/foo/").resolve(URI(".")) === (URI("http://example.com/foo/")))
  }

  "resolveAgainst should work like resolve, just the other way around" in {
    // the following test does not make sense as the resolution base Uri must be absolute
    // URI("http://example.com/foo").resolveAgainst(URI("#bar")) === (URI("http://example.com/foo"))
    assert(URI("bar").resolveAgainst(URI("http://example.com/foo")) === (URI("http://example.com/bar")))
    assert(URI("#bar").resolveAgainst(URI("http://example.com/foo")) === (URI("http://example.com/foo#bar")))
    assert(URI("#bar").resolveAgainst(URI("http://example.com/foo/")) === (URI("http://example.com/foo/#bar")))
    assert(URI("bar").resolveAgainst(URI("http://example.com/foo")) === (URI("http://example.com/bar")))
    assert((URI("bar"): Rdf#Node).resolveAgainst(URI("http://example.com/foo")) === (URI("http://example.com/bar")))
  }

  ".relativize() should relativize the uri against the passed string" in {
    assert(URI("http://example.com/foo").relativize(URI("http://example.com/foo#bar")) === (URI("#bar")))
    assert((URI("http://example.com/foo"): Rdf#Node).relativize(URI("http://example.com/foo#bar")) === (URI("#bar")))
    assert(URI("http://example.com/foo#bar").relativizeAgainst(URI("http://example.com/foo")) === (URI("#bar")))

    assert(URI("http://example.com/ldpc").relativize(URI("http://example.com/ldpc/")) === (URI("")))
    assert(URI("http://example.com/ldpc").relativize(URI("http://example.com/ldpc")) === (URI("")))
    assert(URI("http://example.com/ldpc").relativize(URI("http://example.com/ldpc/entry")) === (URI("entry")))

  }

  "should be able to create and work with relative URIs" in {
    val me = URI("/people/card/henry#me")
    assert(me.fragment === (Some("me")))
    assert(me.fragmentLess === (URI("/people/card/henry")))
    val host = URI("http://bblfish.net")
    assert(me.resolveAgainst(host) === (URI("http://bblfish.net/people/card/henry#me")))
  }

  "transforming java URIs to Rdf#URI" in {
    val card = "http://bblfish.net/people/henry/card"
    val uri: Rdf#URI = URI(card)

    assert(new java.net.URI(card).toUri === (uri))
  }

}

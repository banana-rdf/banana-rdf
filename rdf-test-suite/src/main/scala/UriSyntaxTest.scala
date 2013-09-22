package org.w3.banana.syntax

import org.w3.banana._
import org.w3.banana.diesel._
import org.scalatest._
import java.net.URL

abstract class UriSyntaxTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  ".fragmentLess should remove the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragmentLess should be(URI("http://example.com/foo"))
  }

  ".fragment should set the fragment part of a URI" in {
    val uri = URI("http://example.com/foo")
    uri.fragment("bar") should be(URI("http://example.com/foo#bar"))
  }

  ".fragment should return the fragment part of a URI" in {
    val uri = URI("http://example.com/foo#bar")
    uri.fragment should be(Some("bar"))
    val uriNoFrag = URI("http://example.com/foo")
    uriNoFrag.fragment should be(None)
  }

  "/ should create a sub-resource uri" in {
    (URI("http://example.com/foo") / "bar") should be(URI("http://example.com/foo/bar"))
    (URI("http://example.com/foo/") / "bar") should be(URI("http://example.com/foo/bar"))
  }

  "resolve should resolve the uri against the passed string" in {
    URI("http://example.com/foo").resolve("bar") should be(URI("http://example.com/bar"))
    URI("http://example.com/foo/").resolve("bar") should be(URI("http://example.com/foo/bar"))
  }

  "resolveAgainst should work like resolve, just the other way around" in {
    URI("http://example.com/foo").resolveAgainst(URI("#bar")) should be(URI("http://example.com/foo"))
    /* URI("bar").resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/bar")) */
    URI("#bar").resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/foo#bar"))
    URI("#bar").resolveAgainst(URI("http://example.com/foo/")) should be(URI("http://example.com/foo/#bar"))
    /* URI("bar").resolveAgainst(URI("http://example.com/foo")) should be(URI("http://example.com/bar")) */
  }

  "should be able to create and work with relative URIs" in {
    val me = URI("/people/card/henry#me")
    me.fragment should be(Some("me"))
    me.fragmentLess should be(URI("/people/card/henry"))
    val host = URI("http://bblfish.net")
    me.resolveAgainst(host) should be(URI("http://bblfish.net/people/card/henry#me"))
  }

  "transforming java URIs and URLs to Rdf#URI" in {
    import syntax.URIW
    val card = "http://bblfish.net/people/henry/card"
    val uri: Rdf#URI = URI(card)

    new URL(card).toUri should be(uri)
    new java.net.URI(card).toUri should be(uri)

  }

}

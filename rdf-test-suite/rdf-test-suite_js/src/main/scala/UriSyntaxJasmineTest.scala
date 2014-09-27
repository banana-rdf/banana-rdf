package org.w3.banana.jasmine.test

import org.w3.banana.{ RDFStore => RDFStoreInterface, _ }

import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class UriSyntaxJasmineTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends JasmineTest {

  import ops._

  describe("URI syntax") {

    it(".fragmentLess should remove the fragment part of a URI") {
      val uri = URI("http://example.com/foo#bar")
      expect(uri.fragmentLess == URI("http://example.com/foo")).toEqual(true)
    }

    it(".fragment should set the fragment part of a URI") {
      val uri = URI("http://example.com/foo")
      expect(uri.withFragment("bar") == URI("http://example.com/foo#bar")).toEqual(true)
    }

    it(".fragment should return the fragment part of a URI") {
      val uri = URI("http://example.com/foo#bar")
      expect(uri.fragment == Some("bar")).toEqual(true)
      val uriNoFrag = URI("http://example.com/foo")
      expect(uriNoFrag.fragment == None).toEqual(true)
    }

    it("isPureFragment should should say if a URI is a pure fragment") {
      expect(URI("http://example.com/foo").isPureFragment).toEqual(false)
      expect(URI("http://example.com/foo#bar").isPureFragment).toEqual(false)
      expect(URI("#bar").isPureFragment).toEqual(true)
    }

    it("/ should create a sub-resource uri") {
      expect((URI("http://example.com/foo") / "bar") == URI("http://example.com/foo/bar")).toEqual(true)
      expect((URI("http://example.com/foo/") / "bar") == URI("http://example.com/foo/bar")).toEqual(true)
    }

    it("resolve should resolve the uri against the passed string") {
      expect(URI("http://example.com/foo").resolve(URI("bar")) == URI("http://example.com/bar")).toEqual(true)
      expect(URI("http://example.com/foo").resolve(URI(".")) == URI("http://example.com/")).toEqual(true)
      expect(URI("http://example.com/foo/").resolve(URI("bar")) == URI("http://example.com/foo/bar")).toEqual(true)
    }

    it("resolveAgainst should work like resolve, just the other way around") {
      // the following test does not make sense as the resolution base Uri must be absolute
      // URI("http://example.com/foo").resolveAgainst(URI("#bar")) should be(URI("http://example.com/foo"))
      expect(URI("bar").resolveAgainst(URI("http://example.com/foo")) == URI("http://example.com/bar")).toEqual(true)
      expect(URI("#bar").resolveAgainst(URI("http://example.com/foo")) == URI("http://example.com/foo#bar")).toEqual(true)
      expect(URI("#bar").resolveAgainst(URI("http://example.com/foo/")) == URI("http://example.com/foo/#bar")).toEqual(true)
      expect(URI("bar").resolveAgainst(URI("http://example.com/foo")) == URI("http://example.com/bar")).toEqual(true)
      expect((URI("bar"): Rdf#Node).resolveAgainst(URI("http://example.com/foo")) == URI("http://example.com/bar")).toEqual(true)
    }

    it(".relativize() should relativize the uri against the passed string") {
      expect(URI("http://example.com/foo").relativize(URI("http://example.com/foo#bar")) == URI("#bar")).toEqual(true)
      expect((URI("http://example.com/foo"): Rdf#Node).relativize(URI("http://example.com/foo#bar")) == URI("#bar")).toEqual(true)
      expect(URI("http://example.com/foo#bar").relativizeAgainst(URI("http://example.com/foo")) == URI("#bar")).toEqual(true)
    }

    it("should be able to create and work with relative URIs") {
      val me = URI("/people/card/henry#me")
      expect(me.fragment == Some("me")).toEqual(true)
      expect(me.fragmentLess == URI("/people/card/henry")).toEqual(true)
      val host = URI("http://bblfish.net")
      expect(me.resolveAgainst(host) == URI("http://bblfish.net/people/card/henry#me")).toEqual(true)
    }

    /*
    it("transforming java URIs and URLs to Rdf#URI") {
      import syntax.URIW
      val card = "http://bblfish.net/people/henry/card"
      val uri: Rdf#URI = URI(card)

      new URL(card).toUri should be(uri)
      new java.net.URI(card).toUri should be(uri)
    }
    */
  }
}

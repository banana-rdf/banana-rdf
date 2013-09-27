package org.w3.banana

import org.w3.banana._
import org.scalatest._
import java.io._

abstract class PatchParserTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  "parse variable" in {
    val parser = new PCPatchParser[Rdf]
    val v = parser.parse(parser.varr, new StringReader("?foo"))
    v.get should be (Var("foo"))
  }

  "parse literal" in {
    val parser = new PCPatchParser[Rdf](prefixes = Map(xsd.prefixName -> xsd.prefixIri))
    parser.parse(parser.literal, new StringReader("4")).get should be(TypedLiteral("4", xsd.integer))
    parser.parse(parser.literal, new StringReader(""""foo"""")).get should be(TypedLiteral("foo", xsd.string))
    parser.parse(parser.literal, new StringReader(""""foo"^^xsd:string""")).get should be(TypedLiteral("foo", xsd.string))

  }

  "parse qname or uri" in {
    val parser = new PCPatchParser[Rdf](prefixes = Map(xsd.prefixName -> xsd.prefixIri))
    parser.parse(parser.qnameORuri, new StringReader("""<http://example.com>""")).get should be(URI("http://example.com"))
    parser.parse(parser.qnameORuri, new StringReader("""xsd:foo""")).get should be(xsd("foo"))
  }

  "parse patch query" in {
    val parser = new PCPatchParser[Rdf]
    val query = """
BASE http://example.com/
DELETE {
  <a> <b> ?o
}
INSERT {
  ?s <b> <c>
}
WHERE {
  ?s <b> ?o .
  ?o <b> ?z
}
"""
    val parsed = parser.parse(parser.patch, new StringReader(query)).get
    val expected =
      LDPPatch(
        Some(Delete(TriplesBlock(Vector(TriplePattern(Term(URI("http://example.com/a")),URI("http://example.com/b"),Var("o")))))),
        Some(Insert(TriplesBlock(Vector(TriplePattern(Var("s"),URI("http://example.com/b"),Term(URI("http://example.com/c"))))))),
        Some(Where(TriplesBlock(Vector(
          TriplePattern(Var("s"),URI("http://example.com/b"),Var("o")),
          TriplePattern(Var("o"),URI("http://example.com/b"),Var("z")))))))
    parsed should be(expected)
  }



}

import org.w3.banana.jena._

class JenaPatchParserTest extends PatchParserTest[Jena]

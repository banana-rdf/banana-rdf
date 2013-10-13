package org.w3.banana

import org.w3.banana._
import org.scalatest._
import java.io._

abstract class PatchParserTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  "parse variable" in {
    val parser = new PatchParserCombinator[Rdf]
    val v = parser.parse(parser.varr, new StringReader("?foo"))
    v.get should be (Var("foo"))
  }

  "parse literal" in {
    val parser = new PatchParserCombinator[Rdf](prefixes = Map(xsd.prefixName -> xsd.prefixIri))
    parser.parse(parser.literal, new StringReader("4")).get should be(TypedLiteral("4", xsd.integer))
    parser.parse(parser.literal, new StringReader(""""foo"""")).get should be(TypedLiteral("foo", xsd.string))
    parser.parse(parser.literal, new StringReader(""""foo"^^xsd:string""")).get should be(TypedLiteral("foo", xsd.string))

  }

  "parse qname or uri" in {
    val parser = new PatchParserCombinator[Rdf](prefixes = Map(xsd.prefixName -> xsd.prefixIri))
    parser.parse(parser.qnameORuri, new StringReader("""<http://example.com>""")).get should be(URI("http://example.com"))
    parser.parse(parser.qnameORuri, new StringReader("""xsd:foo""")).get should be(xsd("foo"))
  }

  "parse patch query" in {
    val parser = new PatchParserCombinator[Rdf]
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
      Patch(
        Some(Delete(TriplesPattern(Vector(TriplePattern(Term(URI("http://example.com/a")),IRIRef(URI("http://example.com/b")),Var("o")))))),
        Some(Insert(TriplesPattern(Vector(TriplePattern(Var("s"),IRIRef(URI("http://example.com/b")),Term(URI("http://example.com/c"))))))),
        Some(Where(TriplesBlock(Vector(
          TriplePath(Var("s"),IRIRef(URI("http://example.com/b")),Var("o")),
          TriplePath(Var("o"),IRIRef(URI("http://example.com/b")),Var("z")))))))
    parsed should be(expected)
  }

  "parse patch query with prefixes" in {
    val parser = new PatchParserCombinator[Rdf]
    val query = """
BASE http://example.com/
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
DELETE {
  <a> foaf:b ?o
}
INSERT {
  ?s foaf:b <c>
}
WHERE {
  ?s <b> ?o .
  ?o <b> ?z
}
"""
    val parsed = parser.parse(parser.patch, new StringReader(query)).get
    val expected =
      Patch(
        Some(Delete(TriplesPattern(Vector(TriplePattern(Term(URI("http://example.com/a")),IRIRef(URI("http://xmlns.com/foaf/0.1/b")),Var("o")))))),
        Some(Insert(TriplesPattern(Vector(TriplePattern(Var("s"),IRIRef(URI("http://xmlns.com/foaf/0.1/b")),Term(URI("http://example.com/c"))))))),
        Some(Where(TriplesBlock(Vector(
          TriplePath(Var("s"),IRIRef(URI("http://example.com/b")),Var("o")),
          TriplePath(Var("o"),IRIRef(URI("http://example.com/b")),Var("z")))))))
    parsed should be(expected)
  }

  "all variables under the DELETE clause must be bound in the WHERE clause" in {
    val parser = new PatchParserCombinator[Rdf]
    val query = """
DELETE {
  ?foo <blah> ?o
}
WHERE {
  [] <blah> ?o
}
"""
    intercept[AssertionError] {
      parser.parse(parser.patch, new StringReader(query)).get
    }
  }

  "the BGP in the WHERE clause must be a Tree pattern -- disconnected trees" in {
    val parser = new PatchParserCombinator[Rdf]
    val query = """
DELETE {
  <a> <b> <c>
}
WHERE {
  ?a <p> "foo" .
  ?b <q> "bar"
}
"""
    intercept[AssertionError] {
      parser.parse(parser.patch, new StringReader(query)).get
    }
  }

  "the BGP in the WHERE clause must be a Tree pattern -- tree with 2 subjects" in {
    val parser = new PatchParserCombinator[Rdf]
    val query = """
DELETE {
  <a> <b> <c>
}
WHERE {
  ?a <p> ?c .
  ?b <q> ?c
}
"""
    intercept[AssertionError] {
      parser.parse(parser.patch, new StringReader(query)).get
    }
  }

}

import org.w3.banana.jena._

class JenaPatchParserTest extends PatchParserTest[Jena]

package org.w3.banana

import org.w3.banana.jena.Jena

class Test1 extends munit.FunSuite {
  import org.w3.banana.jena.JenaModule.{*,given}

  test("tests work") {
    assertEquals(true,true)
  }

  test("RDF Prefix test") {
    val rdf = "http://www.w3.org/2000/01/rdf-schema#"
    val rdfs = RDFSPrefix(using Jena)
    assertEquals(rdfs.domain,ops.makeUri(rdf+"domain"))
    assertEquals(rdfs.range,ops.makeUri(rdf+"range"))
  }
}
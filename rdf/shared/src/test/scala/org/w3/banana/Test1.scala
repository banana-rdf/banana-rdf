package org.w3.banana

import org.w3.banana.jena.Jena

class Test1 extends munit.FunSuite {
  import org.w3.banana.jena.JenaModule.{*,given}

  test("tests work") {
    assertEquals(true,true)
  }

  class Outer {
    class Inner
    object InnerObj
    type InnerType
    def process(inner: Inner): Unit = print("hello")
    def processGeneral(inner: Outer#Inner): Unit = print("world")
  }

  //https://blog.rockthejvm.com/scala-3-dependent-types/
  test("path dependent types") {
    val o1 = new Outer()
    val i1 = new o1.Inner
    val o2 = new Outer()
    val i2 = new o2.Inner
    o1.process(i1)
    o1.processGeneral(i2)
  }


//  test("RDF Prefix test") {
//    val rdf = "http://www.w3.org/2000/01/rdf-schema#"
//    val rdfs = RDFSPrefix(using Jena)
//    assertEquals(rdfs.domain,ops.makeUri(rdf+"domain"))
//    assertEquals(rdfs.range,ops.makeUri(rdf+"range"))
//  }
}
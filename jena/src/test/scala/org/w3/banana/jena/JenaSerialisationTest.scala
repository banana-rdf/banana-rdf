package org.w3.banana.jena

import org.w3.banana.Prefix
import org.w3.banana.io.{NTriplesReaderTestSuite, RdfXMLTestSuite, TurtleTestSuite, RDFWriter}
import scala.util.Try
import org.w3.banana.util.tryInstances._

class JenaTurtleTest extends TurtleTestSuite[Jena, Try] {
  "write with prefixes" in {
    val prefix = Set(Prefix[Jena]("foo", "http://purl.org/dc/elements/1.1/"))

//    val expectedString =
//      """
//        |@prefix foo:   <http://purl.org/dc/elements/1.1/> .
//        |
//        |<http://www.w3.org/2001/sw/RDFCore/ntriples/>
//        |        foo:creator    "Dave Beckett" , "Art Barstow" ;
//        |        foo:publisher  <http://www.w3.org/> .""".stripMargin

    val withPrefix = writer.asString(referenceGraph, "", prefix).get
    withPrefix should include("@prefix foo:")
    withPrefix should include("foo:creator")
    withPrefix should include("foo:publisher")
    withPrefix should not include ("<http://purl.org/dc/elements/1.1/creator>")
    withPrefix should not include ("<http://purl.org/dc/elements/1.1/publisher>")

    val noPrefix = writer.asString(referenceGraph, "").get
    noPrefix should not include ("@prefix foo:")
    noPrefix should not include ("foo:creator")
    noPrefix should not include ("foo:publisher")
    noPrefix should include("<http://purl.org/dc/elements/1.1/creator>")
    noPrefix should include("<http://purl.org/dc/elements/1.1/publisher>")

  }
}

class JenaNTripleReaderTestSuite extends NTriplesReaderTestSuite[Jena]

class JenaRdfXMLTest extends RdfXMLTestSuite[Jena, Try]


package org.w3.banana.sesame.io

import org.w3.banana.Prefix
import org.w3.banana.io._
import org.w3.banana.sesame._

import scala.util.Try
import org.w3.banana.util.tryInstances._

class SesameTurtleTests extends TurtleTestSuite[Sesame, Try] {

  "write with prefixes" in {
    val prefix = Set(Prefix[Sesame]("foo", "http://purl.org/dc/elements/1.1/"))

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

class SesameNTripleReaderTestSuite extends NTriplesReaderTestSuite[Sesame]

class SesameNTripleWriterTestSuite extends NTriplesWriterTestSuite[Sesame]

class SesameRdfXMLTests extends RdfXMLTestSuite[Sesame, Try]

class SesameJsonLDCompactedTests extends JsonLDTestSuite[Sesame, Try, JsonLdCompacted]

class SesameJsonLDExpandedTests extends JsonLDTestSuite[Sesame, Try, JsonLdExpanded]

class SesameJsonLDFlattened extends JsonLDTestSuite[Sesame, Try, JsonLdExpanded]

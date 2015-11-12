package org.w3.banana.jena

import org.w3.banana.Prefix
import org.w3.banana.io.{NTriplesReaderTestSuite, RdfXMLTestSuite, TurtleTestSuite, RDFWriter}
import scala.util.Try
import org.w3.banana.util.tryInstances._
import scalaz.syntax._, comonad._

class JenaTurtleTest extends TurtleTestSuite[Jena, Try] {
  "write with prefixes" in {
    val prefix = Prefix[Jena]("foo", "http://purl.org/dc/elements/1.1/")

    val sout = writer.asString(referenceGraph, "", prefix).copoint
//    val expected =
//      """@prefix foo:   <http://purl.org/dc/elements/1.1/> .
//        |
//        |<http://www.w3.org/2001/sw/RDFCore/ntriples/>
//        |        foo:creator    "Dave Beckett" , "Art Barstow" ;
//        |        foo:publisher  <http://www.w3.org/> .""".stripMargin
//    sout must_== expected // TODO this doesn't work as expected for some reason

    val l: Iterator[String] = sout.lines
    l.next() must_== """@prefix foo:   <http://purl.org/dc/elements/1.1/> ."""
    l.next() must_== """"""
    l.next() must_== """<http://www.w3.org/2001/sw/RDFCore/ntriples/>"""
    l.next() must_== """        foo:creator    "Dave Beckett" , "Art Barstow" ;"""
    l.next() must_== """        foo:publisher  <http://www.w3.org/> ."""

    val noPrefix2 = writer.asString(referenceGraph, "").copoint
    val l2 = noPrefix2.lines
    l2.next() must_== """<http://www.w3.org/2001/sw/RDFCore/ntriples/>"""
    l2.next() must_== """        <http://purl.org/dc/elements/1.1/creator>"""
    l2.next() must_== """                "Dave Beckett" , "Art Barstow" ;"""
    l2.next() must_== """        <http://purl.org/dc/elements/1.1/publisher>"""
    l2.next() must_== """                <http://www.w3.org/> ."""
  }
}

class JenaNTripleReaderTestSuite extends NTriplesReaderTestSuite[Jena]

class JenaRdfXMLTest extends RdfXMLTestSuite[Jena, Try]


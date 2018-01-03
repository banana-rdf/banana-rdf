package org.w3.banana.io

import org.w3.banana.{Prefix, RDF, RDFOps}

import scalaz._, scalaz.syntax.comonad._

abstract class PrefixTestSuite[Rdf <: RDF, M[+ _] : Monad : Comonad](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, Turtle],
  val writer: RDFWriter[Rdf, M, Turtle]
) extends SerialisationTestSuite[Rdf, M, Turtle, Turtle]("Turtle", "ttl") {
  val referenceGraphSerialisedForSyntax =
    """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
  """

  "write with prefixes" in {
    val prefix = Set(Prefix[Rdf]("foo", "http://purl.org/dc/elements/1.1/"))

    //    val expectedString =
    //      """
    //        |@prefix foo:   <http://purl.org/dc/elements/1.1/> .
    //        |
    //        |<http://www.w3.org/2001/sw/RDFCore/ntriples/>
    //        |        foo:creator    "Dave Beckett" , "Art Barstow" ;
    //        |        foo:publisher  <http://www.w3.org/> .""".stripMargin

    val withPrefix = writer.asString(referenceGraph, "", prefix).copoint
    withPrefix should include("@prefix foo:")
    withPrefix should include("foo:creator")
    withPrefix should include("foo:publisher")
    withPrefix should not include ("<http://purl.org/dc/elements/1.1/creator>")
    withPrefix should not include ("<http://purl.org/dc/elements/1.1/publisher>")

    val noPrefix = writer.asString(referenceGraph, "").copoint
    noPrefix should not include ("@prefix foo:")
    noPrefix should not include ("foo:creator")
    noPrefix should not include ("foo:publisher")
    noPrefix should include("<http://purl.org/dc/elements/1.1/creator>")
    noPrefix should include("<http://purl.org/dc/elements/1.1/publisher>")

  }

}

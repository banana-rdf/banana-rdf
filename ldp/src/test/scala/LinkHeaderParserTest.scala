package org.w3.banana.ldp

import org.w3.banana._
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import org.w3.banana.plantain.Plantain

class PlantainLinkHeaderParserTest extends LinkHeaderParserTest[Plantain]()(plantain.PlantainOps)


abstract class LinkHeaderParserTest[Rdf<:RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  val lhp = new LinkHeaderParser
  val foaf = FOAFPrefix[Rdf]
  val link = IANALinkPrefix[Rdf]
  val dc = DCPrefix[Rdf]
  val dct = DCTPrefix[Rdf]

  import ops._
  import diesel._
  import syntax._

  "test rel=" in {
    val p1 = lhp.parse("""<.>; rel="collection"""")
    val expected = ( URI("") -- link.collection ->- URI(".") ).graph
    assert (p1.graph isIsomorphicWith expected,s"${p1.graph} must be isomorphic with expected ${expected}")
  }


  "test rel with anchor" in {
    val p1 = lhp.parse("""</>; rel=http://xmlns.com/foaf/0.1/homepage; anchor="#me"""")
    val expected = ( URI("#me") -- foaf.homepage ->- URI("/") ).graph
    assert (p1.graph isIsomorphicWith expected,s"${p1.graph} must be isomorphic with expected ${expected}")
  }


  "test rel no quote with title" in {
    val p2 = lhp.parse("""</TheBook/chapter2>; rel=previous; title*=UTF-8'de'letztes%20Kapitel""")
    val expected = (
      URI("/TheBook/chapter2") -- dct.title ->- LangLiteral("letztes Kapitel",Lang("de"))
      ).graph union (
      URI("") -- link.previous ->- URI("/TheBook/chapter2")
      ).graph
    assert(p2.graph isIsomorphicWith expected,s"${p2.graph} must be isomorphic with expected ${expected}")
  }

  "test two link relations seperated by ','" in {
    val p3 = lhp.parse("""</TheBook/chapter2>; rel="previous"; title*=UTF-8'de'letztes%20Kapitel,
                        | </TheBook/chapter4>; rel="next"; title*=UTF-8'de'n%c3%a4chstes%20Kapitel""".stripMargin)
    val expected = (
       URI("/TheBook/chapter2") -- dct.title ->- LangLiteral("letztes Kapitel",Lang("de"))
      ).graph union (
       URI("") -- link.previous ->- URI("/TheBook/chapter2")
      ).graph union (
       URI("/TheBook/chapter4") -- dct.title ->- LangLiteral("nächstes Kapitel",Lang("de"))
      ).graph union (
       URI("") -- link.next ->- URI("/TheBook/chapter4")
      ).graph
    assert(p3.graph isIsomorphicWith expected,s"${p3.graph} must be isomorphic with expected ${expected}")
  }

  "multiple relations" in {
    val p4 = lhp.parse("""<http://example.org/>;
                        | rel="start http://example.net/relation/other"""".stripMargin)
    val expected = (
      URI("") -- link.start ->- URI("http://example.org/")
              -- URI("http://example.net/relation/other") ->- URI("http://example.org/")
      ).graph

    assert(p4.graph isIsomorphicWith expected,s"${p4.graph} must be isomorphic with expected ${expected}")
  }


}

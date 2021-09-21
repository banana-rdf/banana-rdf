package org.w3.banana.io

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.w3.banana.{PointedGraph, PointedGraphTest, RDF, RDFOps, WebACLPrefix}

import scalaz._
import scalaz.syntax._
import comonad._
import monad._

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, StringReader}


/**
 * Not all formats allow serialisation of graphs with Relative URLs, so here
 * we place tests designed to stress test the implementations of those syntaxes.
 *
 * Relative RDF graphs are essential when POST-ing
 *
 * @param syntax e.g. "Turtle", ...
 * @param extension e.g. "ttl"
 * @param ops
 * @param reader
 * @param writer
 * @tparam Rdf
 * @tparam M is a Monad like Euther or Future, and not a Comonad. That just allows a uniform access to `get`
 *           and so is a terrible hack.
 * @tparam Sin Type for input Syntax
 * @tparam Sout Type of output Syntax
 */
abstract class RelativeGraphSerialisationTestSuite[Rdf <: RDF, M[+_]: Monad : Comonad, Sin, Sout](
  syntax: String,
  extension: String
)(implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, Sin],
  writer: RDFWriter[Rdf, M, Sout]
) extends AnyWordSpec with Matchers {

  import org.w3.banana._

  def gr(pg: PointedGraph[Rdf]): Rdf#Graph = pg.graph

  import ops._

  val wac = WebACLPrefix[Rdf]
  val foaf = FOAFPrefix[Rdf]

  val w3c : Rdf#URI = URI("https://www.w3.org/")
  val timBlcardURI = URI("https://www.w3.org/People/Berners-Lee/card")

  /**
   * we use a realistic example from access control shown in the diagram given in
   *[[https://github.com/solid/authorization-panel/issues/210 issue 210]] of Solid Authorization panel.
   * `rootACL` is meant to be PUT to `</.acl>` of W3C Web server.
   */
  val rootACL = gr(URI("/.acl#Admin").a(wac.Authorization)
    -- wac.mode ->- wac.Control
    -- wac.agent ->- URI("/owner#i")
    -- wac.default ->- URI("/")
  ) union gr(
    URI("/.acl#Public").a(wac.Authorization)
      -- wac.default ->- URI("/")
      -- wac.mode ->- wac.Read
      -- wac.agentClass ->- foaf.Agent
  )

  /**
   * This is the document that is to be POSTed with `Slug: card` onto
   * the container `</People/Berners-Lee/>` creating Tim's WebID. */
  val BLcard = gr(
    URI("#i").a(foaf.Person)
      -- foaf.name ->- "Tim Berners-Lee"
      -- foaf.workInfoHomepage ->- URI("/")
  )

  val timbl = timBlcardURI.withFragment("i")
  val BLcardAbsolute = Graph(
    Triple(timbl, rdf.`type`, foaf.Person),
    Triple(timbl, foaf.name, Literal("Tim Berners-Lee")),
    Triple(timbl, foaf.workInfoHomepage, w3c)
  )

  /**
   * This is the ACL that is meant to be PUT on `</People/Berners-Lee/.acl>`
   */
  val BLAcl   = gr( URI("#TimRl").a(wac.Authorization)
    -- wac.agent ->- URI("card#i")
    -- wac.mode ->- wac.Control
    -- wac.default ->-  URI(".")
  ) union gr(
    URI("") -- owl.imports ->- URI("/.acl")
  )
  s"Writing the empty graph in $syntax" should {
    "not throw an exception" in {
      writer.asString(Graph.empty,None).copoint
    }
  }
  
  s"Writing self references" should {
    val defaultACLGraph: Rdf#Graph = (URI("") -- owl.imports ->- URI(".acl")).graph
    "not throw an exception" in {
      writer.asString(defaultACLGraph,None).copoint
    }
  }

  s"writing relative graphs to $syntax and reading them back from correct base" should {

    "result in isomorphic graphs root container acl" in {
      //1. we build a serialisation with relative URLs
      val rootACLStr: String = writer.asString(rootACL,None).copoint
      //2. after PUTing it to the acl location, we fetch it and parse it with the relative URL location.
      val reconstructedGraph = reader.read(new StringReader(rootACLStr), "https://www.w3.org/.acl").copoint
      val absoluteRootACLGr = rootACL.resolveAgainst(w3c)
      //3. we compare the result with the absolutized graph we should have received
      assert(reconstructedGraph isIsomorphicWith absoluteRootACLGr,
        s"both graphs be isomorphic:\nresult=$reconstructedGraph\nshouldBe=$absoluteRootACLGr")

    }

    "result in isomorphic graph for TimBL's card" in {
      //1. we build a serialisation with relative URLs
      val BLCardStr: String = writer.asString(BLcard,None).copoint
      //2. we POST it to Tim's Personal W3C Container with a Slug "card",
      //   then GET it the newly constructed resource and parse it with the new base,
      val reconstructedGraph = reader.read(new StringReader(BLCardStr), timBlcardURI.toString).copoint
      //3. we compare the result with the absolutized graph we should have received
      assert(reconstructedGraph isIsomorphicWith BLcardAbsolute,
        s"both graphs be isomorphic:\nresult=$reconstructedGraph\nshouldBe=$BLcardAbsolute")
    }

  }

}
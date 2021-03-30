package org.w3.banana.diesel

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.w3.banana._
import org.w3.banana.syntax._
import scalaz.Scalaz.{none, some}

class DieselGraphConstructTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends AnyWordSpec with Matchers {

  import ops._

  val foaf = FOAFPrefix[Rdf]

  "Diesel must accept a GraphNode in the object position" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.title ->- "Mr"
    )

    val expectedGraph =
      Graph(
        Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
        Triple(bnode("betehess"), foaf.title, Literal("Mr"))
      )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true

  }

  "Diesel must construct a simple GraphNode" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.knows ->- (
        URI("http://bblfish.net/#hjs")
        -- foaf.name ->- "Henry Story"
        -- foaf.currentProject ->- URI("http://webid.info/")
      )
    )

    val expectedGraph =
      Graph(
        Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
        Triple(bnode("betehess"), foaf.knows, URI("http://bblfish.net/#hjs")),
        Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story")),
        Triple(URI("http://bblfish.net/#hjs"), foaf.currentProject, URI("http://webid.info/"))
      )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true
  }

  "Diesel must accept triples written in the inverse order o-p-s using <--" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -<- foaf.knows -- (
        URI("http://bblfish.net/#hjs") -- foaf.name ->- "Henry Story"
      )
    )

    val expectedGraph =
      Graph(
        Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
        Triple(URI("http://bblfish.net/#hjs"), foaf.knows, bnode("betehess")),
        Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story"))
      )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true
  }

  "Diesel must allow easy use of rdf:type through the method 'a'" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess").a(foaf.Person)
      -- foaf.name ->- "Alexandre".lang("fr")
    )

    val expectedGraph =
      Graph(
        Triple(bnode("betehess"), rdf("type"), foaf.Person),
        Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr")))
      )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true
  }

  "Diesel must allow objectList definition with simple syntax" in {

    val g: PointedGraph[Rdf] =
      bnode("betehess") -- foaf.name ->- ("Alexandre".lang("fr"), "Alexander".lang("en"))

    val expectedGraph =
      Graph(
        Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
        Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexander", Lang("en")))
      )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true
  }

  "Diesel must allow explicit objectList definition" in {
    val alexs = Seq(
      bnode("a") -- foaf.name ->- "Alexandre".lang("fr"),
      bnode("b") -- foaf.name ->- "Alexander".lang("en")
    )

    val g = (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.knows ->- ObjectList(alexs)
    )

    val expectedGraph =
      Graph(
        Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story")),
        Triple(URI("http://bblfish.net/#hjs"), foaf.knows, bnode("a")),
        Triple(URI("http://bblfish.net/#hjs"), foaf.knows, bnode("b")),
        Triple(bnode("a"), foaf.name, Literal.tagged("Alexander", Lang("en"))),
        Triple(bnode("b"), foaf.name, Literal.tagged("Alexandre", Lang("fr")))
      )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true
  }

  "Diesel with empty explicit objectList definition" in {
    val g = (
      URI("http://bblfish.net/#hjs")
        -- foaf.name ->- "Henry Story"
        -- foaf.knows ->- ObjectList(Seq.empty[Int])
    )

    val expectedGraph = Graph(
      Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story"))
    )

    (g.graph isIsomorphicWith expectedGraph)  shouldEqual true
  }

  "Diesel must understand Scala's native types" in {

    val g = (
      bnode("betehess")
      -- foaf.name ->- "Alexandre"
      -- foaf.age ->- 29
      -- foaf.height ->- 1.80
    ).graph

    val expectedGraph =
      Graph(
        Triple(bnode("betehess"), foaf.name, Literal("Alexandre", xsd.string)),
        Triple(bnode("betehess"), foaf.age, Literal("29", xsd.integer)),
        Triple(bnode("betehess"), foaf.height, Literal("1.8", xsd.double))
      )

    (g isIsomorphicWith expectedGraph)
  }

  "Diesel must support RDF collections" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess")
      -- foaf.name ->- List(1, 2, 3)
    )

    val l: PointedGraph[Rdf] = (
      bnode()
      -- rdf.first ->- 1
      -- rdf.rest ->- (
        bnode()
        -- rdf.first ->- 2
        -- rdf.rest ->- (
          bnode()
          -- rdf.first ->- 3
          -- rdf.rest ->- rdf.nil
        )
      )
    )

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- l
    )

    (g.graph isIsomorphicWith expectedGraph.graph)  shouldEqual true
  }

  "Diesel must support RDF collections (empty list)" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess") -- foaf.name ->- List[String]()
    )

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- rdf.nil
    )

    (g.graph isIsomorphicWith expectedGraph.graph)  shouldEqual true
  }

  "providing a None as an object does not emit a triple" in {

    val g = (
      bnode("betehess")
      -- foaf.name ->- "Alexandre"
      -- foaf.age ->- none[Int]
    ).graph

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- "Alexandre"
    ).graph

    (g isIsomorphicWith expectedGraph)  shouldEqual true

  }

  "test JSON Literals" in {
    val jwtRsa = """{
						|   "kty" : "RSA",
						|   "n"   : "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx
						|            4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs
						|            tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2
						|            QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI
						|            SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb
						|            w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
						|   "e"   : "AQAB",
						|   "alg" : "RS256",
						|   "kid" : "2011-04-29"
						| }""".stripMargin

    val cert = CertPrefix[Rdf]
    val keyId = URI("http://alice.example/key#i")
    val jwtPg = keyId -- cert.key ->- Literal(jwtRsa, rdf.JSON)

    val expectedGraph =
      Graph(
        Triple(keyId, cert.key, Literal(jwtRsa, rdf.JSON)),
      )

    assert(jwtPg.graph isIsomorphicWith expectedGraph) shouldEqual true
  }

  "providing a Some(t) as an object just emits the triple with t as an object" in {

    val g = (
      bnode("betehess")
      -- foaf.name ->- "Alexandre"
      -- foaf.age ->- some(42)
    ).graph

    val expectedGraph = (
      bnode("b")
      -- foaf.name ->- "Alexandre"
      -- foaf.age ->- 42
    ).graph

    (g isIsomorphicWith expectedGraph) shouldEqual true

  }

  "disconnected graph construction" in {

    val g = (
      bnode("a") -- foaf.name ->- "Alexandre"
      -- foaf.age ->- 29
    ).graph union (
        bnode("h") -- foaf.name ->- "Henry"
        -- foaf.height ->- 1.92
      ).graph

    val expectedGraph =
      Graph(
        Triple(bnode("a"), foaf.name, Literal("Alexandre", xsd.string)),
        Triple(bnode("a"), foaf.age, Literal("29", xsd.integer)),
        Triple(bnode("h"), foaf.name, Literal("Henry", xsd.string)),
        Triple(bnode("h"), foaf.height, Literal("1.92", xsd.double))
      )

    (g.graph isIsomorphicWith expectedGraph) shouldEqual true

  }

  "Diesel must support sets" in {

    val pg: PointedGraph[Rdf] = (
      bnode("betehess") -- foaf.name ->- Set(
        1.toPG,
        "blah".toPG,
        bnode("foo") -- foaf.homepage ->- URI("http://example.com")
      )
    )

    val expectedGraph = Graph(Set(
      Triple(bnode("betehess"), foaf.name, Literal("1", xsd.integer)),
      Triple(bnode("betehess"), foaf.name, Literal("blah")),
      Triple(bnode("betehess"), foaf.name, bnode("foo")),
      Triple(bnode("foo"), foaf.homepage, URI("http://example.com"))
    ))

    (pg.graph isIsomorphicWith expectedGraph) shouldEqual true
  }

}

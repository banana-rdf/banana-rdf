package org.w3.banana.jasmine.test

import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.w3.banana.binder._
import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scalaz.Scalaz._
import scala.util._
import scala.collection.immutable.ListMap
import java.io._
import scala.concurrent.Future
import org.w3.banana.{ RDFStore => RDFStoreInterface }

import scala.scalajs.js
import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class DieselGraphConstructJasmineTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends JasmineTest {
  import ops._
  import syntax._

  val foaf = FOAFPrefix[Rdf]

  describe("Diesel ops") {

    it("Diesel must accept a GraphNode in the object position") {

      val g: PointedGraph[Rdf] = (
        bnode("betehess")
        -- foaf.name ->- "Alexandre".lang("fr")
        -- foaf.title ->- "Mr"
      )

      val expectedGraph =
        Graph(
          Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
          Triple(bnode("betehess"), foaf.title, Literal("Mr")))

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)

    }

    it("Diesel must construct a simple GraphNode") {

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
          Triple(URI("http://bblfish.net/#hjs"), foaf.currentProject, URI("http://webid.info/")))

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel must accept triples written in the inverse order o-p-s using <--") {

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

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel must allow easy use of rdf:type through the method 'a'") {

      val g: PointedGraph[Rdf] = (
        bnode("betehess").a(foaf.Person)
        -- foaf.name ->- "Alexandre".lang("fr")
      )

      val expectedGraph =
        Graph(
          Triple(bnode("betehess"), rdf("type"), foaf.Person),
          Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))))

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel must allow objectList definition with simple syntax") {

      val g: PointedGraph[Rdf] =
        bnode("betehess") -- foaf.name ->- ("Alexandre".lang("fr"), "Alexander".lang("en"))

      val expectedGraph =
        Graph(
          Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexandre", Lang("fr"))),
          Triple(bnode("betehess"), foaf.name, Literal.tagged("Alexander", Lang("en")))
        )

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel must allow explicit objectList definition") {
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

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel with empty explicit objectList definition") {
      val g =
        (
          URI("http://bblfish.net/#hjs")
          -- foaf.name ->- "Henry Story"
          -- foaf.knows ->- ObjectList(Seq.empty[Int])
        )

      val expectedGraph =
        Graph(
          Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story"))
        )
      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel must understand Scala's native types") {

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
          Triple(bnode("betehess"), foaf.height, Literal("1.8", xsd.double)))
      expect(g isIsomorphicWith expectedGraph).toEqual(true)
    }

    it("Diesel must support RDF collections") {

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
      expect(g.graph isIsomorphicWith expectedGraph.graph).toEqual(true)
    }

    it("Diesel must support RDF collections (empty list)") {

      val g: PointedGraph[Rdf] = (
        bnode("betehess") -- foaf.name ->- List[String]()
      )

      val expectedGraph = (
        bnode("betehess") -- foaf.name ->- rdf.nil
      )

      expect(g.graph isIsomorphicWith expectedGraph.graph).toEqual(true)
    }

    it("providing a None as an object does not emit a triple") {

      val g = (
        bnode("betehess")
        -- foaf.name ->- "Alexandre"
        -- foaf.age ->- none[Int]
      ).graph

      val expectedGraph = (
        bnode("betehess") -- foaf.name ->- "Alexandre"
      ).graph

      expect(g isIsomorphicWith expectedGraph).toEqual(true)

    }

    it("providing a Some(t) as an object just emits the triple with t as an object") {

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

      expect(g isIsomorphicWith expectedGraph).toEqual(true)

    }

    it("disconnected graph construction") {

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

      expect(g.graph isIsomorphicWith expectedGraph).toEqual(true)

    }

    it("Diesel must support sets") {

      val pg: PointedGraph[Rdf] = (
        bnode("betehess") -- foaf.name ->- Set(1.toPG,
          "blah".toPG,
          bnode("foo") -- foaf.homepage ->- URI("http://example.com"))
      )

      val expectedGraph = Graph(Set(
        Triple(bnode("betehess"), foaf.name, Literal("1", xsd.integer)),
        Triple(bnode("betehess"), foaf.name, Literal("blah")),
        Triple(bnode("betehess"), foaf.name, bnode("foo")),
        Triple(bnode("foo"), foaf.homepage, URI("http://example.com"))
      ))

      expect(pg.graph isIsomorphicWith expectedGraph).toEqual(true)
    }
  }

}

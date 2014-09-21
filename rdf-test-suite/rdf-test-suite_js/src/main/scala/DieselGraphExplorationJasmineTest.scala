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
abstract class DieselGraphExplorationJasmineTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends JasmineTest {
  import ops._
  import syntax._

  val foaf = FOAFPrefix[Rdf]

  val betehess: PointedGraph[Rdf] = (
    URI("http://bertails.org/#betehess").a(foaf.Person)
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.name ->- "Alexander".lang("en")
    -- foaf.age ->- 29
    -- foaf("foo") ->- List(1, 2, 3)
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs").a(foaf.Person)
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  )

  describe("Traversals") {

    it("'/' method must traverse the graph") {
      val names = betehess / foaf.name
      expect(names.map(_.pointer).toSet == Set(Literal.tagged("Alexandre", Lang("fr")), Literal.tagged("Alexander", Lang("en"))))
    }

    it("'/' method must work with uris and bnodes") {

      val name = betehess / foaf.knows / foaf.name

      expect(name.head.pointer.equals(Literal("Henry Story"))).toEqual(true)

    }

    it("we must be able to project nodes to Scala types") {

      expect((betehess / foaf.age).as[Int] == Success(29)).toEqual(true)

      expect((betehess / foaf.knows / foaf.name).as[String] == Success("Henry Story")).toEqual(true)

    }

    it("betehess should have three predicates: foaf:name foaf:age foaf:knows") {

      val predicates = betehess.predicates.toList
      List(foaf.name, foaf.age, foaf.knows) foreach { p => expect(predicates.contains(p)) }

    }

    it("we must be able to get rdf lists") {

      expect((betehess / foaf("foo")).as[List[Int]] == Success(List(1, 2, 3))).toEqual(true)

    }

    it("we must be able to optionally get objects") {
      expect((betehess / foaf.age).asOption[Int] == Success(Some(29))).toEqual(true)

      expect((betehess / foaf.age).asOption[String].isFailure).toEqual(true)

      expect((betehess / foaf("unknown")).asOption[Int] == Success(None)).toEqual(true)

    }

    it("asking for one (or exactly one) node when there is none must fail") {

      expect((betehess / foaf("unknown")).takeOnePointedGraph.isFailure).toEqual(true)

      expect((betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure).toEqual(true)

    }

    it("asking for exactly one node when there are more than one must fail") {

      expect((betehess / foaf.name).exactlyOnePointedGraph.isFailure).toEqual(true)

    }

    it("asking for one node when there is at least one must be a success") {

      expect((betehess / foaf.name).takeOnePointedGraph.isSuccess).toEqual(true)

      expect((betehess / foaf.age).takeOnePointedGraph.isSuccess).toEqual(true)

    }

    it("asking for exactly one pointed graph when there is none must fail") {

      expect((betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure).toEqual(true)

    }

    it("asking for exactly one pointed graph when there are more than one must fail") {

      expect((betehess / foaf.name).exactlyOnePointedGraph.isFailure).toEqual(true)

    }

    it("getAllInstancesOf must give all instances of a given class") {

      val persons = betehess.graph.getAllInstancesOf(foaf.Person).nodes

      expect(persons.toSet == Set(URI("http://bertails.org/#betehess"), URI("http://bblfish.net/#hjs"))).toEqual(true)

    }

    it("isA must test if a node belongs to a class") {

      expect(betehess.isA(foaf.Person)).toEqual(true)

      expect(betehess.isA(foaf("SomethingElse"))).toEqual(false)

    }
  }
}

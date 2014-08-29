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
import org.scalatest.EitherValues._
import scala.concurrent.Future
import org.w3.banana.{ RDFStore => RDFStoreInterface }

import scala.scalajs.js
import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class SparqlEngineJasmineTest[Rdf <: RDF](store: RDFStore[Rdf])(
  implicit reader: RDFReader[Rdf, Turtle],
  ops: RDFOps[Rdf],
  sparqlOps: SparqlOps[Rdf])
    extends JasmineTest {

  import ops._
  import syntax._
  import sparqlOps._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

  val foaf = FOAFPrefix(ops)

  val graph1: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  ).graph

  describe("SPARQL  Operations") {

    it("new-tr.rdf must have Alexandre Bertails as an editor") {

      jasmine.Clock.useMock()

      val graphs: Array[Any] = new Array[Any](1)

      val rdfStore = GraphStore[Rdf](store)

      rdfStore.appendToGraph(URI("http://example.com/graph1"), graph1)
      jasmine.Clock.tick(10)
      rdfStore.appendToGraph(URI("http://example.com/graph2"), graph2)
      jasmine.Clock.tick(10)

      val query = SelectQuery("""
                                 |prefix : <http://www.w3.org/2001/02pd/rec54#>
                                 |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                                 |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                                 |prefix foaf: <http://xmlns.com/foaf/0.1/>
                                 |
                                 |SELECT DISTINCT ?name WHERE {
                                 |  graph <http://example.com/graph1> {
                                 |    ?ed foaf:name ?name
                                 |  }
                                 |}""".stripMargin)

      val sparqlEngine = SparqlEngine[Rdf](store)
      val res: Future[Rdf#Solutions] = sparqlEngine.executeSelect(query)
      jasmine.Clock.tick(10)
      res.onSuccess {
        case rows =>
          var c = 0
          rows.toIterable.map {
            row =>
              c = c + 1
              expect(row("name").get.toString).toEqual("\"Alexandre\"@fr")
          }
          expect(c == 1).toEqual(true)
      }
      res.onFailure {
        case r =>
          throw r
      }
      jasmine.Clock.tick(10)
      jasmine.Clock.tick(10)

    }

    it("the identity Sparql Construct must work as expected") {
      jasmine.Clock.useMock()

      val query = ConstructQuery(
        """
          |CONSTRUCT {
          |  ?s ?p ?o
          |} WHERE {
          |  graph <http://example.com/graph1> {
          |    ?s ?p ?o
          |  }
          |}""".stripMargin)

      val sparqlEngine = SparqlEngine[Rdf](store)
      val res: Future[Rdf#Graph] = sparqlEngine.executeConstruct(query)
      jasmine.Clock.tick(10)
      res.onSuccess {
        case g => {
          expect(g isIsomorphicWith graph1).toEqual(true)
        }
      }
    }

    it("Alexandre Bertails must appear as an editor in new-tr.rdf") {
      jasmine.Clock.useMock()

      val query = AskQuery("""
                             |prefix : <http://www.w3.org/2001/02pd/rec54#>
                             |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                             |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                             |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
                             |prefix foaf: <http://xmlns.com/foaf/0.1/>
                             |
                             |ASK {
                             |  graph <http://example.com/graph1> {
                             |    ?ed foaf:name ?name .
                             |  }
                             |}""".stripMargin)

      val sparqlEngine = SparqlEngine[Rdf](store)
      val res: Future[Boolean] = sparqlEngine.executeAsk(query)
      jasmine.Clock.tick(10)
      res.onSuccess {
        case b => {
          expect(b).toEqual(true)
        }
      }
    }

    it("Henry Story must have banana-rdf as current-project") {
      jasmine.Clock.useMock()
      var c = 0

      val query = UpdateQuery(
        """
          |prefix foaf: <http://xmlns.com/foaf/0.1/>
          |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
          |
          |INSERT {
          | GRAPH <http://example.com/graph2> {
          |   ?author foaf:name "Alex"
          | }
          |} WHERE {
          | GRAPH <http://example.com/graph2> {
          |   ?author foaf:name "Alexandre"@fr
          | }
          |}
        """.stripMargin
      )
      val sparqlEngine = SparqlEngine[Rdf](store)
      val res = sparqlEngine.executeUpdate(query)
      jasmine.Clock.tick(10)
      res.onSuccess {
        case _ =>
          val result = sparqlEngine.executeSelect(SelectQuery(
            """
              |prefix foaf: <http://xmlns.com/foaf/0.1/>
              |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
              |
              |SELECT ?name
              |WHERE {
              | GRAPH <http://example.com/graph2> {
              |   ?author foaf:name ?name
              | }
              |}
            """.stripMargin)
          )

          result.onSuccess {
            case rows =>
              rows.toIterable.map {
                row =>
                  c += 1

                  expect(row("name").get.toString.equals("\"Alex\"") ||
                    row("name").get.toString.equals("\"Alexandre\"@fr") ||
                    row("name").get.toString.equals("\"Henry Story\"^^<http://www.w3.org/2001/XMLSchema#string>")).toEqual(true)
              }

          }
      }
      jasmine.Clock.tick(10)
      expect(c == 3).toEqual(true)
    }

  }
}

package org.w3.banana

import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.scalatest._
import scala.concurrent._
import scala.concurrent.util._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.FileInputStream

class SparqlEngineTest[Rdf <: RDF, A](
  val store: A)(
    implicit val reader: RDFReader[Rdf, RDFXML],
    val ops: RDFOps[Rdf],
    val sparqlOps: SparqlOps[Rdf],
    val graphStore: GraphStore[Rdf, A],
    val sparqlEngine: SparqlEngine[Rdf, A],
    val lifecycle: Lifecycle[Rdf, A])
    extends WordSpec with SparqlEngineTesterTrait[Rdf, A] with Matchers with BeforeAndAfterAll with TryValues {

  import ops._
  import sparqlOps._
  import graphStore.graphStoreSyntax._
  import sparqlEngine.sparqlEngineSyntax._
  import lifecycle.lifecycleSyntax._

  "new-tr.rdf must have Alexandre Bertails as an editor" in {

    val query = parseSelect("""
                           |prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |
                           |SELECT DISTINCT ?name WHERE {
                           |  graph <http://example.com/graph> {
                           |    ?thing :editor ?ed .
                           |    ?ed contact:fullName ?name
                           |  }
                           |}""".stripMargin).success.value

    val names: Iterable[String] =
      store.executeSelect(query).getOrFail().iterator.to[Iterable].map {
        row => row("name").success.value.as[String].success.value
      }

    names should contain("Alexandre Bertails")

  }

  "new-tr.rdf must have Alexandre Bertails as an editor (with-bindings version)" in {

    val query = parseSelect("""
                           |prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |
                           |SELECT DISTINCT ?name WHERE {
                           |  graph ?g {
                           |    ?thing :editor ?ed .
                           |    ?ed ?prop ?name
                           |  }
                           |}""".stripMargin).success.value

    val bindings = Map("g" -> URI("http://example.com/graph"),
      "thing" -> URI("http://www.w3.org/TR/2012/CR-rdb-direct-mapping-20120223/"),
      "prop" -> URI("http://www.w3.org/2000/10/swap/pim/contact#fullName"))

    val names: Iterable[String] =
      store.executeSelect(query, bindings).getOrFail().iterator.to[Iterable].map {
        row => row("name").success.value.as[String].success.value
      }

    names should have size (4)

    names should contain("Alexandre Bertails")

  }

  "the identity Sparql Construct must work as expected" in {

    val query = parseConstruct("""
                              |CONSTRUCT {
                              |  ?s ?p ?o
                              |} WHERE {
                              |  graph <http://example.com/graph> {
                              |    ?s ?p ?o
                              |  }
                              |}""".stripMargin).success.value

    val clonedGraph = store.executeConstruct(query).getOrFail()

    assert(clonedGraph isIsomorphicWith graph)
  }

  "Alexandre Bertails must appear as an editor in new-tr.rdf" in {

    val query = parseAsk("""
                        |prefix : <http://www.w3.org/2001/02pd/rec54#>
                        |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                        |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
                        |
                        |ASK {
                        |  graph <http://example.com/graph> {
                        |    ?thing :editor ?ed .
                        |    ?ed contact:fullName "Alexandre Bertails"^^xsd:string
                        |  }
                        |}""".stripMargin).success.value

    val alexIsThere = store.executeAsk(query).getOrFail()

    alexIsThere should be(true)

  }

  "Alexandre Bertails must appear as an editor in new-tr.rdf (with-bindings version)" in {

    val query = parseAsk("""
                        |prefix : <http://www.w3.org/2001/02pd/rec54#>
                        |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                        |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
                        |
                        |ASK {
                        |  graph ?g {
                        |    ?thing :editor ?ed .
                        |    ?ed ?prop ?name
                        |  }
                        |}""".stripMargin).success.value
    val bindings = Map(
      "g" -> URI("http://example.com/graph"),
      "thing" -> URI("http://www.w3.org/TR/2012/CR-rdb-direct-mapping-20120223/"),
      "prop" -> URI("http://www.w3.org/2000/10/swap/pim/contact#fullName"),
      "name" -> "Alexandre Bertails".toNode)

    val alexIsThere = store.executeAsk(query, bindings).getOrFail()

    alexIsThere should be(true)

  }

  "betehess must know henry" in {

    val query = parseAsk("""
                        |prefix foaf: <http://xmlns.com/foaf/0.1/>
                        |ASK {
                        |  GRAPH <http://example.com/graph2> {
                        |    [] foaf:knows <http://bblfish.net/#hjs>
                        |  }
                        |}""".stripMargin).success.value

    val alexKnowsHenry = store.executeAsk(query).getOrFail()

    alexKnowsHenry should be(true)

  }

}

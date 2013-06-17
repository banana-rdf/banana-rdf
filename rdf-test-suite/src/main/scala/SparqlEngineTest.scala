package org.w3.banana

import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers._
import scala.concurrent._
import scala.concurrent.util._
import scalax.io._
import scala.concurrent.ExecutionContext.Implicits.global

class SparqlEngineTest[Rdf <: RDF](
  store: RDFStore[Rdf, Future])(
    implicit reader: RDFReader[Rdf, RDFXML],
    ops: RDFOps[Rdf],
    sparqlOps: SparqlOps[Rdf])
    extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import ops._
  import sparqlOps._

  val sparqlEngine = SparqlEngine[Rdf, Future](store)

  override def afterAll(): Unit = {
    super.afterAll()
    store.shutdown()
  }

  val foaf = FOAFPrefix(ops)

  val resource = Resource.fromFile("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(resource, "http://example.com") getOrElse sys.error("ouch")

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

  override def beforeAll(): Unit = {

    val init = store.execute {
      for {
        _ <- Command.append(URI("http://example.com/graph1"), graph1.toIterable)
        _ <- Command.append(URI("http://example.com/graph2"), graph2.toIterable)
        _ <- Command.append(URI("http://example.com/graph"), graph.toIterable)
      } yield ()
    }
    init.getOrFail()

  }

  "new-tr.rdf must have Alexandre Bertails as an editor" in {

    val query = SelectQuery("""
                           |prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |
                           |SELECT DISTINCT ?name WHERE {
                           |  graph <http://example.com/graph> {
                           |    ?thing :editor ?ed .
                           |    ?ed contact:fullName ?name
                           |  }
                           |}""".stripMargin)

    val names: Future[Iterable[String]] = sparqlEngine.executeSelect(query).map(_.toIterable.map {
      row => row("name").flatMap(_.as[String]) getOrElse sys.error("")
    })

    names.getOrFail() must contain("Alexandre Bertails")

  }

  "new-tr.rdf must have Alexandre Bertails as an editor (with-bindings version)" in {

    val query = SelectQuery("""
                           |prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |
                           |SELECT DISTINCT ?name WHERE {
                           |  graph ?g {
                           |    ?thing :editor ?ed .
                           |    ?ed ?prop ?name
                           |  }
                           |}""".stripMargin)

    val bindings = Map("g" -> URI("http://example.com/graph"),
      "thing" -> URI("http://www.w3.org/TR/2012/CR-rdb-direct-mapping-20120223/"),
      "prop" -> URI("http://www.w3.org/2000/10/swap/pim/contact#fullName"))

    val names: Future[Iterable[String]] = sparqlEngine.executeSelect(query, bindings).map(_.toIterable.map {
      row => row("name").flatMap(_.as[String]) getOrElse sys.error("")
    })

    names.getOrFail() must have size (4)

    names.getOrFail() must contain("Alexandre Bertails")

  }

  "the identity Sparql Construct must work as expected" in {

    val query = ConstructQuery("""
                              |CONSTRUCT {
                              |  ?s ?p ?o
                              |} WHERE {
                              |  graph <http://example.com/graph> {
                              |    ?s ?p ?o
                              |  }
                              |}""".stripMargin)

    val clonedGraph = sparqlEngine.executeConstruct(query).getOrFail()

    assert(clonedGraph isIsomorphicWith graph)
  }

  "Alexandre Bertails must appear as an editor in new-tr.rdf" in {

    val query = AskQuery("""
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
                        |}""".stripMargin)

    val alexIsThere = sparqlEngine.executeAsk(query).getOrFail()

    alexIsThere must be(true)

  }

  "Alexandre Bertails must appear as an editor in new-tr.rdf (with-bindings version)" in {

    val query = AskQuery("""
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
                        |}""".stripMargin)
    val bindings = Map(
      "g" -> URI("http://example.com/graph"),
      "thing" -> URI("http://www.w3.org/TR/2012/CR-rdb-direct-mapping-20120223/"),
      "prop" -> URI("http://www.w3.org/2000/10/swap/pim/contact#fullName"),
      "name" -> "Alexandre Bertails".toNode)

    val alexIsThere = sparqlEngine.executeAsk(query, bindings).getOrFail()

    alexIsThere must be(true)

  }

  "betehess must know henry" in {

    val query = AskQuery("""
                        |prefix foaf: <http://xmlns.com/foaf/0.1/>
                        |ASK {
                        |  GRAPH <http://example.com/graph2> {
                        |    [] foaf:knows <http://bblfish.net/#hjs>
                        |  }
                        |}""".stripMargin)

    val alexKnowsHenry = sparqlEngine.executeAsk(query).getOrFail()

    alexKnowsHenry must be(true)

  }

  "Henry Story must have banana-rdf as current-project" in {
    val query = UpdateQuery(
      """
        |prefix foaf: <http://xmlns.com/foaf/0.1/>
        |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
        |
        |INSERT {
        | GRAPH <http://example.com/graph2> {
        |   ?author foaf:currentProject <http://github.com/w3c/banana-rdf>
        | }
        |} WHERE {
        | GRAPH <http://example.com/graph2> {
        |   ?author foaf:name "Henry Story"^^xsd:string
        | }
        |}
      """.stripMargin
    )

    sparqlEngine.executeUpdate(query).getOrFail()
    val result = sparqlEngine.executeSelect(SelectQuery(
      """
        |prefix foaf: <http://xmlns.com/foaf/0.1/>
        |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
        |
        |SELECT ?currentProject
        |WHERE {
        | GRAPH <http://example.com/graph2> {
        |   ?author foaf:name "Henry Story"^^xsd:string .
        |   ?author foaf:currentProject ?currentProject
        | }
        |}
      """.stripMargin)
    ).map(_.toIterable.map(
      row => row("currentProject").flatMap(_.as[Rdf#URI]) getOrElse sys.error("")
    )).getOrFail()

    result must have size (2)
    result must contain(URI("http://github.com/w3c/banana-rdf"))
  }
}

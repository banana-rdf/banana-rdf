package org.w3.banana

import akka.actor.Status.Success
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import org.scalatest._
import java.io._

class SparqlGraphTest[Rdf <: RDF, SyntaxType]()(
  implicit ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, RDFXML],
  sparqlOperations: SparqlOps[Rdf],
  sparqlGraph: SparqlEngine[Rdf, Rdf#Graph],
  sparqlWriter: SparqlSolutionsWriter[Rdf, SyntaxType],
  sparqlReader: SparqlQueryResultsReader[Rdf, SyntaxType])
    extends WordSpec with Matchers with Inside with TryValues {

  import ops._
  import sparqlOperations._
  import sparqlGraph.sparqlEngineSyntax._

  val foaf = FOAFPrefix[Rdf]

  val resource = new FileInputStream("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(resource, "http://foo.com").success.value

  "SELECT DISTINCT query in new-tr.rdf " should {
    val selectQueryStr = """prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |
                           |SELECT DISTINCT ?name WHERE {
                           |  ?thing :editor ?ed .
                           |  ?ed contact:fullName ?name
                           |}
                         """.stripMargin

    def testAnswer(solutions: Rdf#Solutions) = {
      val rows = solutions.iterator.to[List]

      val names: List[String] = rows.map {
        row => row("name").success.value.as[String].success.value
      }

      names should contain("Alexandre Bertails")

      val row = rows(0)
      row("unknown") should be('failure)
      true
    }

    "have Alexandre Bertails as an editor" in {
      val query = parseSelect(selectQueryStr).success.value
      val answers: Rdf#Solutions = graph.executeSelect(query).getOrFail()
      testAnswer(answers)
    }

    "the sparql answer should serialise and deserialise " in {
      val query = parseSelect(selectQueryStr).success.value
      //in any case we must re-execute query, as the results returned can often only be read once
      val answers = graph.executeSelect(query).getOrFail()

      val out = new ByteArrayOutputStream()

      val serialisedAnswer = sparqlWriter.write(answers, out, "")
      assert(serialisedAnswer.isSuccess, "the sparql must be serialisable")

      val answr2 = sparqlReader.read(new ByteArrayInputStream(out.toByteArray), "")
      assert(answr2.isSuccess, "the serialised sparql answers must be deserialisable")

      answr2.map(a => assert(testAnswer(a.left.get), "the deserialised answer must pass the same tests as the original one"))
    }
  }

  "the identity Sparql Construct " should {

    val query = parseConstruct("""
                   |CONSTRUCT {
                   |  ?s ?p ?o
                   |} WHERE {
                   |  ?s ?p ?o
                   |}
                   |""".stripMargin).success.value

    "work as expected " in {

      val clonedGraph = graph.executeConstruct(query).getOrFail()

      assert(clonedGraph isIsomorphicWith graph)
    }

  }

  "ASK Query on simple graph" should {

    val simple: PointedGraph[Rdf] = (
      bnode("thing") -- URI("http://www.w3.org/2001/02pd/rec54#editor") ->- (bnode("i")
        -- foaf.name ->- "Henry".lang("en")))

    val yesQuery = parseAsk("ASK { ?thing <http://xmlns.com/foaf/0.1/name> ?name }").success.value
    val noQuery = parseAsk("ASK { ?thing <http://xmlns.com/foaf/0.1/knows> ?name }").success.value
    val yesQuery2 = parseAsk(
      """| Prefix : <http://www.w3.org/2001/02pd/rec54#>
         | ASK { ?thing :editor [ <http://xmlns.com/foaf/0.1/name> ?name ] }""".stripMargin).success.value

    "simple graph contains at least one named person" in {
      val personInFoaf = simple.graph.executeAsk(yesQuery).getOrFail()
      assert(personInFoaf, " query " + yesQuery + " must return true")
    }

    "simple graph contains no foaf:knows relation" in {
      val knowRelInFoaf = simple.graph.executeAsk(noQuery).getOrFail()
      assert(!knowRelInFoaf, " query " + noQuery + " must return false")
    }

    "more advanced query is ok" in {
      val objectHasNamedEditor = simple.graph.executeAsk(yesQuery2).getOrFail()
      assert(objectHasNamedEditor, " query " + yesQuery2 + " must return true")
    }

  }

  "ASK Query on new-tr.rdf" should {

    val query = parseAsk("""
                           |prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
                           |
                           |ASK {
                           |  ?thing :editor ?ed .
                           |  ?ed contact:fullName "Alexandre Bertails"^^xsd:string .
                           |}""".stripMargin).success.value

    "Alexandre Bertails must appear as an editor in new-tr.rdf" in { //was: taggedAs (SesameWIP)
      val alexIsThere = graph.executeAsk(query).getOrFail()

      assert(alexIsThere, " query " + query + " must return true")
    }
  }

  "a Sparql query constructor must accept Prefix objects" in {

    val query1 = parseConstruct("""
          |prefix : <http://www.w3.org/2001/02pd/rec54#>
          |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
          |
          |CONSTRUCT {
          |  ?thing :editor ?ed .
          |  ?ed contact:fullName ?name .
          |}  WHERE {
          |  ?thing :editor ?ed .
          |  ?ed contact:fullName ?name
          |}
          |""".stripMargin).success.value

    val base = Prefix[Rdf]("", "http://www.w3.org/2001/02pd/rec54#")
    val rdf = RDFPrefix[Rdf]
    val contact = Prefix[Rdf]("contact", "http://www.w3.org/2000/10/swap/pim/contact#")

    val query2parsed = parseConstruct("""
                               |CONSTRUCT {
                               |  ?thing :editor ?ed .
                               |  ?ed contact:fullName ?name .
                               |}  WHERE {
                               |  ?thing :editor ?ed .
                               |  ?ed contact:fullName ?name
                               |}""".stripMargin, Seq(base, rdf, contact))
    query2parsed should be('Success)
    val query2 = query2parsed.success.value

    val contructed1 = graph.executeConstruct(query1).getOrFail()
    val constructed2 = graph.executeConstruct(query2).getOrFail()

    assert(contructed1 isIsomorphicWith constructed2, "the results of both queries should be isomorphic")
  }

}

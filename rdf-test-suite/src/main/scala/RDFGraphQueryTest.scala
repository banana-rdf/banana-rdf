package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, OutputStreamWriter, StringWriter }

class RDFGraphQueryTest[Rdf <: RDF, SyntaxType]()(
    implicit diesel: Diesel[Rdf],
    reader: BlockingReader[Rdf#Graph, RDFXML],
    sparqlOperations: SPARQLOps[Rdf],
    graphQuery: RDFGraphQuery[Rdf],
    sparqlWriter: SparqlSolutionsWriter[Rdf, SyntaxType],
    sparqlReader: SparqlQueryResultsReader[Rdf, SyntaxType]) extends WordSpec with MustMatchers with Inside {

  import diesel._
  import sparqlOperations._

  val file = new java.io.File("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(file, "http://foo.com") getOrElse sys.error("ouch")
  val sparqlEngine = graphQuery.makeSPARQLEngine(graph)

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
      val rows = solutions.toIterable.toList

      val names: List[String] = rows map {
        row => row("name").flatMap(_.as[String]) getOrElse sys.error("")
      }

      names must contain("Alexandre Bertails")

      val row = rows(0)
      row("unknown") must be('failure)
      true
    }

    "have Alexandre Bertails as an editor" in {
      val query = SelectQuery(selectQueryStr)
      val answers: Rdf#Solutions = sparqlEngine.executeSelect(query)
      testAnswer(answers)
    }

    "the sparql answer should serialise and deserialise " in {
      val query = SelectQuery(selectQueryStr)
      //in any case we must re-execute query, as the results returned can often only be read once
      val answers = sparqlEngine.executeSelect(query)

      val out = new ByteArrayOutputStream()

      val serialisedAnswer = sparqlWriter.write(answers, out, "")
      assert(serialisedAnswer.isSuccess, "the sparql must be serialisable")

      val answr2 = sparqlReader.read(new ByteArrayInputStream(out.toByteArray), "")
      assert(answr2.isSuccess, "the serialised sparql answers must be deserialisable")

      answr2.map(a => assert(testAnswer(a.left.get), "the deserialised answer must pass the same tests as the original one"))
    }
  }

  "the identity SPARQL Construct " should {

    val query = ConstructQuery("""
                   |CONSTRUCT {
                   |  ?s ?p ?o
                   |} WHERE {
                   |  ?s ?p ?o
                   |}
                   |""".stripMargin)

    "work as expected " in {

      val clonedGraph = sparqlEngine.executeConstruct(query)

      assert(clonedGraph isIsomorphicWith graph)
    }

  }

  "ASK Query on simple graph" should {

    val simple: PointedGraph[Rdf] = (
      bnode("thing") -- URI("http://www.w3.org/2001/02pd/rec54#editor") ->- (bnode("i")
        -- foaf.name ->- "Henry".lang("en")
      )
    )

    val yesQuery = AskQuery("ASK { ?thing <http://xmlns.com/foaf/0.1/name> ?name }")
    val noQuery = AskQuery("ASK { ?thing <http://xmlns.com/foaf/0.1/knows> ?name }")
    val yesQuery2 = AskQuery(
      """| Prefix : <http://www.w3.org/2001/02pd/rec54#>
         | ASK { ?thing :editor [ <http://xmlns.com/foaf/0.1/name> ?name ] }""".stripMargin)

    "simple graph contains at least one named person" in {
      val personInFoaf = graphQuery.makeSPARQLEngine(simple.graph).executeAsk(yesQuery)
      assert(personInFoaf, " query " + yesQuery + " must return true")
    }

    "simple graph contains no foaf:knows relation" in {
      val knowRelInFoaf = graphQuery.makeSPARQLEngine(simple.graph).executeAsk(noQuery)
      assert(!knowRelInFoaf, " query " + noQuery + " must return false")
    }

    "more advanced query is ok" in {
      val objectHasNamedEditor = graphQuery.makeSPARQLEngine(simple.graph).executeAsk(yesQuery2)
      assert(objectHasNamedEditor, " query " + yesQuery2 + " must return true")
    }

  }

  "ASK Query on new-tr.rdf" should {

    val query = AskQuery("""
                           |prefix : <http://www.w3.org/2001/02pd/rec54#>
                           |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                           |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
                           |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
                           |
                           |ASK {
                           |  ?thing :editor ?ed .
                           |  ?ed contact:fullName "Alexandre Bertails"^^xsd:string .
                           |}""".stripMargin)

    "Alexandre Bertails must appear as an editor in new-tr.rdf" in { //was: taggedAs (SesameWIP)
      val alexIsThere = sparqlEngine.executeAsk(query)

      assert(alexIsThere, " query " + query + " must return true")
    }

    "the sparql answer should serialise and deserialise " in {
      //in any case we must re-execute query, as the results returned can often only be read once
      val answers = sparqlEngine.executeAsk(query)

      val out = new ByteArrayOutputStream()

      val serialisedAnswer = BooleanWriter.WriterSelector(MediaRange(sparqlWriter.syntax.mime)).map {
        l =>
          l.write(answers, out, "")
      }.getOrElse(fail("could not find sparql boolean writer for " + sparqlWriter.syntax.mime))

      assert(serialisedAnswer.isSuccess, "the sparql must be serialisable")

      val answr2 = sparqlReader.read(new ByteArrayInputStream(out.toByteArray), "")
      assert(answr2.isSuccess, "the serialised sparql answers must be deserialisable ")

      answr2.map { a =>
        assert(a.isRight, "The answer to a ASK is a boolean")
        val result = a.right.get
        assert(result, " query " + query + "must return true")
      }
    }
  }

  //taggedAs (SesameWIP)
  "a SPARQL query constructor must accept Prefix objects" in {

    val query1 = ConstructQuery("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

CONSTRUCT {
  ?thing :editor ?ed .
  ?ed contact:fullName ?name .
}  WHERE {
  ?thing :editor ?ed .
  ?ed contact:fullName ?name
}
                             """)

    val base = Prefix[Rdf]("", "http://www.w3.org/2001/02pd/rec54#")
    val rdf = RDFPrefix[Rdf]
    val contact = Prefix[Rdf]("contact", "http://www.w3.org/2000/10/swap/pim/contact#")

    val query2 = ConstructQuery("""
                               |CONSTRUCT {
                               |  ?thing :editor ?ed .
                               |  ?ed contact:fullName ?name .
                               |}  WHERE {
                               |  ?thing :editor ?ed .
                               |  ?ed contact:fullName ?name
                               |}""".stripMargin, base, rdf, contact)

    val contructed1 = sparqlEngine.executeConstruct(query1)
    val constructed2 = sparqlEngine.executeConstruct(query2)

    assert(contructed1 isIsomorphicWith constructed2, "the results of both queries should be isomorphic")
  }

}

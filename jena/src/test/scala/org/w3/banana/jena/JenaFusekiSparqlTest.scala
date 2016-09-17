package org.w3.banana.jena

import java.net.URL

import org.apache.jena.tdb.TDBFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}
import org.w3.banana._

/**
 * Sparql update test on a Fuseki embedded server
 */
class JenaFusekiSparqlTest extends FlatSpec
  with Matchers with BeforeAndAfterAll with JenaModule{

  lazy val data = "rdf-test-suite/jvm/src/main/resources/known-tr-editors.rdf"

  lazy val server: FusekiServer = new FusekiServer(dataset = TDBFactory.createDataset(), dataFiles = List(data))

  import ops._
  import sparqlHttp.sparqlEngineSyntax._
  import sparqlOps._

  /**
   * Start Fuseki server
   */
  override def beforeAll(): Unit = {
    server.start
  }

  /**
   * Stop server
   */
  override def afterAll(): Unit = {
    server.stop
  }

  /*"The repository"*/ignore must "contain person 'Morgana'" in {

    val sparqlUpdate =
      """
        |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        |PREFIX c: <http://www.w3.org/2000/10/swap/pim/contact#>
        |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
        |
        |INSERT DATA {
        |   _:node1040 rdf:type c:Person .
        |   _:node1040 c:firstName "Morgana" .
        |   _:node1040 c:lastName "Ramalho" .
        |   _:node1040 c:sortName "Ramalho" .
        |}
      """.stripMargin

    val url = new URL("http://localhost:3030/ds/update")
    val query = parseUpdate(sparqlUpdate).get
    val updateEndpoint = new JenaSparqlHttpEngine
    updateEndpoint.executeUpdate(url, query, Map()).get

    val selectQuery = parseSelect(
      """
        |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        |    PREFIX c: <http://www.w3.org/2000/10/swap/pim/contact#>
        |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
        |
        |    SELECT ?firstName
        |    WHERE {
        |       ?node rdf:type c:Person .
        |       ?node c:firstName ?firstName .
        |       ?node c:lastName ?lastName .
        |       ?node c:firstName "Morgana" .
        |    }
      """.stripMargin).get

    val client = new URL("http://localhost:3030/ds/query")
    val results = client.executeSelect(selectQuery).get.iterator.to[Iterable]
    val result = results.map(
      row => row("firstName").get
    )

    result should have size (1)
    result.head.getLiteralLexicalForm should be ("Morgana")
  }

}

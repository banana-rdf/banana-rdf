package org.w3.banana

import java.net.URL

import com.hp.hpl.jena.tdb.TDBFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, _}

/**
 * Sparql update test on a Fuseki embedded server
 */
abstract class FusekiSparqlTest[Rdf <: RDF]()
  (implicit
    val ops: RDFOps[Rdf],
    val sparqlOps: SparqlOps[Rdf],
    val sparqlHttp: SparqlEngine[Rdf, URL] with SparqlUpdate[Rdf, URL])
  extends  FlatSpec with Matchers with BeforeAndAfterAll {

  import ops._
  import sparqlOps._

  val data = "rdf-test-suite/jvm/src/main/resources/known-tr-editors.rdf"

  val server: FusekiServer = new FusekiServer(dataset = TDBFactory.createDataset(), dataFiles = List(data))

  /**
   * Start Fuseki server
   */
  override def beforeAll: Unit = {
    server.start
  }

  "The repository" must "contain person 'Morgana'" in {

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

    val endpointUpdate = new URL("http://localhost:3030/ds/update")
    val query = parseUpdate(sparqlUpdate).get
    sparqlHttp.executeUpdate(endpointUpdate, query, Map.empty).getOrFail()

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

    val endpointQuery = new URL("http://localhost:3030/ds/query")
    val results = sparqlHttp.executeSelect(endpointQuery, selectQuery, Map.empty).getOrFail().iterator.to[Iterable]
    val result = results.map(
      row => row("firstName").get
    )

    result should have size (1)
    //result.head.getLiteralLexicalForm should be ("Morgana")
  }

  /**
   * Stop server
   */
  override def afterAll: Unit = {
    server.stop
  }
}

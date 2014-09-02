package org.w3.banana

import org.scalatest.{ TryValues, BeforeAndAfterAll, Matchers, WordSpec }

/**
 * Sparql Update gets a special test as not all servers implement it.
 */
class SparqlUpdateEngineTest[Rdf <: RDF, A](
  val store: A)(
    implicit val reader: RDFReader[Rdf, RDFXML],
    val ops: RDFOps[Rdf],
    val sparqlOps: SparqlOps[Rdf],
    val graphStore: GraphStore[Rdf, A],
    val sparqlUpdateEngine: SparqlEngine[Rdf, A] with SparqlUpdate[Rdf, A],
    val lifecycle: Lifecycle[Rdf, A])
    extends WordSpec with SparqlEngineTesterTrait[Rdf, A] with Matchers with BeforeAndAfterAll with TryValues {
  import ops._
  import sparqlOps._
  import graphStore.graphStoreSyntax._
  import sparqlUpdateEngine.sparqlUpdateSyntax._
  import sparqlUpdateEngine.sparqlEngineSyntax._
  import lifecycle.lifecycleSyntax._
  import org.w3.banana.diesel._

  "Henry Story must have banana-rdf as current-project" in {
    val updateQuery = parseUpdate(
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
    ).success.value

    store.executeUpdate(updateQuery).getOrFail()

    val selectQuery = parseSelect(
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
        """.stripMargin).success.value

    val projects = store.executeSelect(selectQuery).getOrFail().iterator.to[Iterable]
    val result = projects.map(
      row => row("currentProject").success.value.as[Rdf#URI].success.value
    )

    result should have size (2)
    result should contain(URI("http://github.com/w3c/banana-rdf"))
  }
}

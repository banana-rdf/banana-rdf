package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._

abstract class SparqlTest[Rdf <: RDF, Sparql <: SPARQL](
  ops: RDFOperations[Rdf],
  reader: RDFReader[Rdf, RDFXML],
  iso: GraphIsomorphism[Rdf],
  queryBuilder: SPARQLQueryBuilder[Rdf, Sparql],
  queryExecution: RDFGraphQuery[Rdf, Sparql]
) extends WordSpec with MustMatchers {

  val projections = RDFNodeProjections(ops)

  import ops._
  import iso._
  import queryBuilder._
  import queryExecution._

  val file = new java.io.File("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(file, "http://foo.com") getOrElse sys.error("ouch")

  "new-tr.rdf must have Alexandre Bertails as an editor" in {

    val query = SelectQuery("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

SELECT DISTINCT ?name WHERE {
  ?thing :editor ?ed .
  ?ed contact:fullName ?name
}
""")

    val names: Iterable[String] = executeSelectQuery(graph, query) map { row => projections.asString(getNode(row, "name")) getOrElse sys.error("") }

    names must contain ("Alexandre Bertails")

  }



  "the identity SPARQL Construct must work as expected" in {

    val query = ConstructQuery("""
CONSTRUCT {
  ?s ?p ?o
} WHERE {
  ?s ?p ?o
}
""")

    val clonedGraph = executeConstructQuery(graph, query)

    assert(clonedGraph isIsomorphicWith graph)

  }


  "Alexandre Bertails must appear as an editor in new-tr.rdf" in {

    val query = AskQuery("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

ASK {
  ?thing :editor ?ed .
  ?ed contact:fullName "Alexandre Bertails"
}
""")

    val alexIsThere = executeAskQuery(graph, query)

    alexIsThere must be (true)

  }




  "a SPARQL query constructor must accept Prefix objects" in {

    val query1 = SelectQuery("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

SELECT DISTINCT ?name WHERE {
  ?thing :editor ?ed .
  ?ed contact:fullName ?name
}
""")

    val base = Prefix("", "http://www.w3.org/2001/02pd/rec54#", ops)
    val rdf = RDFPrefix(ops)
    val contact = Prefix("contact", "http://www.w3.org/2000/10/swap/pim/contact#", ops)

    val query2 = SelectQuery("""
SELECT DISTINCT ?name WHERE {
  ?thing :editor ?ed .
  ?ed contact:fullName ?name
}
""", base, rdf, contact)

    query1 must be === query2

  }


}

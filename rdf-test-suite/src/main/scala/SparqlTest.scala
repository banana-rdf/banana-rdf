package org.w3.rdf

import org.scalatest._
import org.scalatest.matchers._

abstract class SparqlTest[Rdf <: RDF](
  ops: RDFOperations[Rdf],
  reader: RDFReader[Rdf, RDFXML],
  sparql: Sparql[Rdf]
) extends WordSpec with MustMatchers {

  val projections = RDFNodeProjections(ops)

  import ops._
  import sparql._

  "new-tr.rdf must have Alexandre Bertails as an editor" in {

    val file = new java.io.File("rdf-test-suite/src/main/resources/new-tr.rdf")

    val graph = reader.read(file, "http://foo.com") getOrElse sys.error("ouch")

    val query = Select("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

SELECT DISTINCT ?name WHERE {
  ?thing :editor ?ed .
  ?ed contact:fullName ?name
}
""")

    val names: Iterable[String] = executeSelect(graph, query) map { row => projections.asString(getNode(row, "name")) getOrElse sys.error("") }

    names must contain ("Alexandre Bertails")

  }

}

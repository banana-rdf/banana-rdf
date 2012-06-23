package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStreamWriter, StringWriter}
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream

abstract class RDFGraphQueryTest[Rdf <: RDF, Sparql <: SPARQL, SyntaxType](
  ops: RDFOperations[Rdf],
  diesel: Diesel[Rdf],
  reader: RDFReader[Rdf, RDFXML],
  iso: GraphIsomorphism[Rdf],
  sparqlOperations: SPARQLOperations[Rdf, Sparql],
  graphQuery: RDFGraphQuery[Rdf, Sparql],
  sparqlWriter: BlockingSparqlAnswerWriter[Sparql,SyntaxType],
  sparqlReader: BlockingSparqlAnswerReader[Sparql,SyntaxType]
) extends WordSpec with MustMatchers with Inside {

  import ops._
  import diesel._
  import iso._
  import sparqlOperations._
  import graphQuery._

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
    val answers = executeSelect(graph,query)

    def testAnswer(solutions: Sparql#Solutions) {
      val rows = solutions.toIterable.toList

      val names: List[String] = rows map {
        row => row("name").flatMap(_.as[String]) getOrElse sys.error("")
      }

      names must contain("Alexandre Bertails")

      val row = rows(0)
      row("unknown") must be ('failure)
    }
    testAnswer(answers)

    val answers2 = executeSelect(graph,query) //we re-execute the query, as the underlying query often returns a read once structure
    val out = new ByteArrayOutputStream()
    val serialisedAnswer = sparqlWriter.write(answers2,out)

    serialisedAnswer.isSuccess must be (true)

    val answr2 = sparqlReader.read(new ByteArrayInputStream(out.toByteArray))

    answr2.isSuccess must be (true)

    answr2.map(a=>testAnswer(a))


  }



  "the identity SPARQL Construct must work as expected" in {

    val query = ConstructQuery("""
CONSTRUCT {
  ?s ?p ?o
} WHERE {
  ?s ?p ?o
}
""")

    val clonedGraph = executeConstruct(graph, query)

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

    val alexIsThere = executeAsk(graph, query)

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

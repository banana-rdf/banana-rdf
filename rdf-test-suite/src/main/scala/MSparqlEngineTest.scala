package org.w3.banana

import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import scalaz._
import Scalaz._
import util.UnsafeExtractor


abstract class MSparqlEngineTest[Rdf <: RDF, M[_]](implicit diesel: Diesel[Rdf],
                                                   sparqlOps: SPARQLOperations[Rdf],
                                                   reader: BlockingReader[Rdf#Graph, RDFXML],
                                                   bind: Bind[M],
                                                   extractor: UnsafeExtractor[M]) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  def store: MRDFStore[Rdf, M]


  import diesel._
  import sparqlOps._
  import extractor._


  val file = new java.io.File("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(file, "http://example.com") getOrElse sys.error("ouch")

  "new-tr.rdf must have Alexandre Bertails as an editor" in {

    val query = SelectQuery( """
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

SELECT DISTINCT ?name WHERE {
  graph <http://example.com/graph> {
    ?thing :editor ?ed .
    ?ed contact:fullName ?name
  }
}
                             """)

    val names: M[Iterable[String]] = store.executeSelect(query).map(_.toIterable.map {
      row => row("name").flatMap(_.as[String]) getOrElse sys.error("")
    })

    unsafeExtract(names).map(_ must contain("Alexandre Bertails")).fail.map(throw _)

  }

  "the identity SPARQL Construct must work as expected" in {

    val query = ConstructQuery( """
CONSTRUCT {
  ?s ?p ?o
} WHERE {
  graph <http://example.com/graph> {
    ?s ?p ?o
  }
}
                                """)

    val clonedGraph = unsafeExtract(store.executeConstruct(query))

    clonedGraph.map(g => assert(g isIsomorphicWith graph)) must be('success)
  }

  "Alexandre Bertails must appear as an editor in new-tr.rdf" in {

    val query = AskQuery( """
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

ASK {
  graph <http://example.com/graph> {
    ?thing :editor ?ed .
    ?ed contact:fullName "Alexandre Bertails"
  }
}
                          """)

    val alexIsThere = unsafeExtract(store.executeAsk(query))


    alexIsThere.map(_ must be(true)) must be('success)

  }


  val foaf = FOAFPrefix(ops)

  val graph1: Rdf#Graph = (
    bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.title ->- "Mr"
    ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.knows ->- (
      uri("http://bblfish.net/#hjs")
        -- foaf.name ->- "Henry Story"
        -- foaf.currentProject ->- uri("http://webid.info/")
      )
    ).graph

  override def beforeAll(): Unit = {
    store.addNamedGraph(uri("http://example.com/graph1"), graph1)
    store.addNamedGraph(uri("http://example.com/graph2"), graph2)
    store.addNamedGraph(uri("http://example.com/graph"), graph)
  }

  "betehess must know henry" in {

    val query = AskQuery( """
prefix foaf: <http://xmlns.com/foaf/0.1/>

ASK {
  GRAPH <http://example.com/graph2> {
    [] foaf:knows <http://bblfish.net/#hjs>
  }
}
                          """)

    val alexKnowsHenry = store.executeAsk(query)

    import extractor._

    unsafeExtract(alexKnowsHenry.map(_ must be(true))).fail.map(throw _)

  }

}


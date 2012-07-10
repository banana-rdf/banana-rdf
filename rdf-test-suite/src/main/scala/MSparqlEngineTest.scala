package org.w3.banana

import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import scalaz._
import Scalaz._
import util.UnsafeExtractor



abstract class MSparqlEngineTest[Rdf <: RDF, M[_]](implicit diesel: Diesel[Rdf],
    sparqlOps: SPARQLOperations[Rdf],
    bind: Bind[M],
    extractor: UnsafeExtractor[M])  extends WordSpec with MustMatchers with BeforeAndAfterAll {

  def store: MRDFStore[Rdf,M]


  import diesel._
  import ops._
  import sparqlOps._

  val foaf = FOAFPrefix(ops)

  val graph: Rdf#Graph = (
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
    store.addNamedGraph(uri("http://example.com/graph"), graph)
    store.addNamedGraph(uri("http://example.com/graph2"), graph2)
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


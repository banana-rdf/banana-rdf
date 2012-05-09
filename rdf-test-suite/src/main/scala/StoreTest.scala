package org.w3.banana

import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers._

abstract class StoreTest[Rdf <: RDF](
  ops: RDFOperations[Rdf],
  dsl: Diesel[Rdf],
  graphUnion: GraphUnion[Rdf],
  rdfStore: RDFStore[Rdf],
  reader: RDFReader[Rdf, RDFXML],
  iso: GraphIsomorphism[Rdf]
  // queryBuilder: SPARQLQueryBuilder[Rdf, Sparql],
  // queryExecution: SPARQLGraphQueryExecution[Rdf, Sparql]
) extends WordSpec with MustMatchers {

//  val projections = RDFNodeProjections(ops)

  import rdfStore._
  import iso._
  import ops._
  import dsl._
  import graphUnion._

  val store: Rdf#Store
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

  "getNamedGraph should retrieve the graph added with addNamedGraph" in {
    addNamedGraph(store, IRI("http://example.com/graph"), graph)
    addNamedGraph(store, IRI("http://example.com/graph2"), graph2)
    val retrievedGraph = getNamedGraph(store, IRI("http://example.com/graph"))
    val retrievedGraph2 = getNamedGraph(store, IRI("http://example.com/graph2"))
    assert(graph isIsomorphicWith retrievedGraph)
    assert(graph2 isIsomorphicWith retrievedGraph2)
  }

  "appendToNamedGraph should be equivalent to graph union" in {
    addNamedGraph(store, IRI("http://example.com/graph"), graph)
    appendToNamedGraph(store, IRI("http://example.com/graph"), graph2)
    val retrievedGraph = getNamedGraph(store, IRI("http://example.com/graph"))
    val unionGraph = union(graph, graph2)
    assert(unionGraph isIsomorphicWith retrievedGraph)
  }


}

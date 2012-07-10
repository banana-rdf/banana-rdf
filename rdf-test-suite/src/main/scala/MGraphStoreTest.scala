package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import scalaz._
import Scalaz._


abstract class MGraphStoreTest[Rdf <: RDF, M[_]](implicit diesel: Diesel[Rdf],
                                                 reader: BlockingReader[Rdf#Graph, RDFXML],
                                                 bind: Bind[M]) extends WordSpec with MustMatchers {

  def store: MGraphStore[Rdf,M]

  import diesel._
  import ops._

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

  val foo: Rdf#Graph = (
    uri("http://example.com/foo")
      -- rdf("foo") ->- "foo"
      -- rdf("bar") ->- "bar"
    ).graph

  "getNamedGraph should retrieve the graph added with addNamedGraph" in {
    store.addNamedGraph(uri("http://example.com/graph"), graph)
    store.addNamedGraph(uri("http://example.com/graph2"), graph2)
    val retrievedGraph = store.getNamedGraph(uri("http://example.com/graph"))
    val retrievedGraph2 = store.getNamedGraph(uri("http://example.com/graph2"))

    retrievedGraph.map(r => assert(graph isIsomorphicWith r))
    retrievedGraph2.map(r => assert(graph2 isIsomorphicWith r))
  }

  "appendToNamedGraph should be equivalent to graph union" in {
    store.addNamedGraph(uri("http://example.com/graph"), graph)
    store.appendToNamedGraph(uri("http://example.com/graph"), graph2)
    val retrievedGraph = store.getNamedGraph(uri("http://example.com/graph"))
    val unionGraph = union(graph :: graph2 :: Nil)
    retrievedGraph.map(r => assert(unionGraph isIsomorphicWith r))
  }

  "addNamedGraph should drop the existing graph" in {
    val u = uri("http://example.com/graph")

    store.addNamedGraph(u, foo)
    store.addNamedGraph(u, graph)
    val retrievedGraph = store.getNamedGraph(u)

    assert(!(graph isIsomorphicWith foo))

    retrievedGraph.map(r => r isIsomorphicWith graph)
  }


}
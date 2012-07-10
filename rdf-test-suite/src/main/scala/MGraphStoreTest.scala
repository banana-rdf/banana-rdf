package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import scalaz._
import Scalaz._
import util.UnsafeExtractor

abstract class MGraphStoreTest[Rdf <: RDF, M[_]](implicit diesel: Diesel[Rdf],
    reader: BlockingReader[Rdf#Graph, RDFXML],
    bind: Bind[M],
    extractor: UnsafeExtractor[M]) extends WordSpec with MustMatchers {

  def store: MGraphStore[Rdf, M]

  import diesel._
  import ops._
  import extractor._

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
    unsafeExtract(store.addNamedGraph(uri("http://example.com/graph"), graph).flatMap(
      _ => store.addNamedGraph(uri("http://example.com/graph2"), graph2).flatMap(
        _ => store.getNamedGraph(uri("http://example.com/graph")).flatMap (
           rGraph => store.getNamedGraph(uri("http://example.com/graph2")).map(
             rGraph2 => {
               assert(rGraph isIsomorphicWith graph)
               assert(rGraph2 isIsomorphicWith graph2)
             }
           )
        )
      )
    )) must be('success)
  }

  "appendToNamedGraph should be equivalent to graph union" in {
    unsafeExtract(store.addNamedGraph(uri("http://example.com/graph"), graph).flatMap(
      _ => store.appendToNamedGraph(uri("http://example.com/graph"), graph2).flatMap(
        _ => store.getNamedGraph(uri("http://example.com/graph")).map(
          rGraph => assert(rGraph isIsomorphicWith union(graph :: graph2 :: Nil))
        )
      )
    )) must be('success)
  }

  "addNamedGraph should drop the existing graph" in {
    val u = uri("http://example.com/graph")

    //If graph and foo are isomorphic, the test means nothing
    assert(!(graph isIsomorphicWith foo))

    unsafeExtract(store.addNamedGraph(u, foo).flatMap(
      _ => store.addNamedGraph(u, graph).flatMap(
        _ => store.getNamedGraph(u).map(
          rGraph => assert(rGraph isIsomorphicWith graph)
        )
      )
    )) must be('success)

  }

}
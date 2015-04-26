package org.w3.banana

import org.scalatest.{Matchers, WordSpec}


class MGraphTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  val ex = Prefix[Rdf]("ex", "http://example.com/")

  // note: not defining a reference graph on purpose, as it could rely
  // on a mutable implementations
  val triples: List[Rdf#Triple] = List(
    Triple(BNode("foo"), ex("bar"), ex("baz")),
    Triple(BNode("foo"), ex("qux"), Literal("foobar"))
  )

  "MGraph" should {

    "An empty MGraph must yield an empty Graph" in {
      (makeEmptyMGraph().makeIGraph() isIsomorphicWith Graph.empty)  shouldEqual true
    }

    "An MGraph must accept new triples" in {
      val mgraph = makeEmptyMGraph()
      triples.foreach(triple => mgraph += triple)
      (mgraph.makeIGraph() isIsomorphicWith Graph(triples))  shouldEqual true
      ((makeEmptyMGraph() ++= triples).makeIGraph() isIsomorphicWith Graph(triples))  shouldEqual true
    }

    "Going back and forth between Graph and MGraph must work" in {
      (Graph(triples).makeMGraph().makeIGraph() isIsomorphicWith Graph(triples))  shouldEqual true
    }

    // in some implementations, Rdf#Graph and Rdf#MGraph can share
    // references, so we have to make sure that defensive copies are
    // done in the case of mutable Graphs
    "Operations on an MGraph obtained from an existing Graph must have no incidence on original Graph" in {
      val originalGraph = Graph(triples)
      val mgraph = originalGraph.makeMGraph()
      mgraph += Triple(ex("foo"), ex("bar"), ex("baz"))
      (originalGraph isIsomorphicWith Graph(triples))  shouldEqual true
    }

    "An MGraph must be able to remove existing triples" in {
      val mgraph = Graph(triples).makeMGraph()
      mgraph -= Triple(BNode("foo"), ex("bar"), ex("baz"))
      mgraph.makeIGraph() isIsomorphicWith Graph(Triple(BNode("foo"), ex("qux"), Literal("foobar")))
    }

    "Trying to remove a non-existing triple must not fail" in {
      val mgraph = Graph(triples).makeMGraph()
      mgraph -= Triple(ex("does"), ex("not"), ex("exist"))
      (mgraph.makeIGraph() isIsomorphicWith Graph(triples))  shouldEqual true
    }

    "MGraph knows if a triple was already added" in {
      val mgraph = Graph(triples).makeMGraph()
      (! mgraph.exists(Triple(ex("does"), ex("not"), ex("exist"))))  shouldEqual true
      mgraph.exists(Triple(BNode("foo"), ex("bar"), ex("baz")))  shouldEqual true
    }

  }

}

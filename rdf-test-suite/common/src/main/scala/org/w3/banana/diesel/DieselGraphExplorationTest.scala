package org.w3.banana.diesel

import org.scalatest.WordSpec
import org.w3.banana._
import org.w3.banana.syntax._

import scala.util._

class DieselGraphExplorationTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec {

  import ops._

  val foaf = FOAFPrefix[Rdf]

  val betehess: PointedGraph[Rdf] = (
    URI("http://bertails.org/#betehess").a(foaf.Person)
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.name ->- "Alexander".lang("en")
    -- foaf.age ->- 29
    -- foaf("foo") ->- List(1, 2, 3)
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs").a(foaf.Person)
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  )

  "'/' method must traverse the graph" in {
    val names = betehess / foaf.name
    assert(names.map(_.pointer).toSet === (Set(Literal.tagged("Alexandre", Lang("fr")), Literal.tagged("Alexander", Lang("en")))))
  }

  "'/' method must work with uris and bnodes" in {

    val name = betehess / foaf.knows / foaf.name

    assert(name.head.pointer === (Literal("Henry Story")))

  }

  "we must be able to project nodes to Scala types" in {

    assert((betehess / foaf.age).as[Int] === (Success(29)))

    assert((betehess / foaf.knows / foaf.name).as[String] === (Success("Henry Story")))

  }

  "betehess should have three predicates: foaf:name foaf:age foaf:knows" in {

    val predicates = betehess.predicates.toList
    List(foaf.name, foaf.age, foaf.knows) foreach { p => assert(predicates.contains(p)) }

  }

  "we must be able to get rdf lists" in {

    assert((betehess / foaf("foo")).as[List[Int]] === (Success(List(1, 2, 3))))

  }

  "we must be able to optionally get objects" in {

    assert((betehess / foaf.age).asOption[Int] === (Success(Some(29))))

    assert((betehess / foaf.age).asOption[String].isFailure)

    assert((betehess / foaf("unknown")).asOption[Int] === (Success(None)))

  }

  "asking for one (or exactly one) node when there is none must fail" in {

    assert((betehess / foaf("unknown")).takeOnePointedGraph.isFailure)

    assert((betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure)

  }

  "asking for exactly one node when there are more than one must fail" in {

    assert((betehess / foaf.name).exactlyOnePointedGraph.isFailure)

  }

  "asking for one node when there is at least one must be a success" in {

    assert((betehess / foaf.name).takeOnePointedGraph.isSuccess)

    assert((betehess / foaf.age).takeOnePointedGraph.isSuccess)

  }

  "asking for exactly one pointed graph when there is none must fail" in {

    assert((betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure)

  }

  "asking for exactly one pointed graph when there are more than one must fail" in {

    assert((betehess / foaf.name).exactlyOnePointedGraph.isFailure)

  }

  "getAllInstancesOf must give all instances of a given class" in {

    val persons = betehess.graph.getAllInstancesOf(foaf.Person).nodes

    assert(persons.toSet === (Set(URI("http://bertails.org/#betehess"), URI("http://bblfish.net/#hjs"))))

  }

  "isA must test if a node belongs to a class" in {

    assert(betehess.isA(foaf.Person))

    assert(! betehess.isA(foaf("SomethingElse")))

  }

}

package org.w3.banana.diesel

import org.w3.banana._
import org.w3.banana.syntax._
import org.scalatest._
import java.io._
import org.scalatest.EitherValues._
import scala.util._

abstract class DieselGraphExplorationTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with Matchers {

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
    names.map(_.pointer).toSet should be(Set(Literal.tagged("Alexandre", Lang("fr")), Literal.tagged("Alexander", Lang("en"))))
  }

  "'/' method must work with uris and bnodes" in {

    val name = betehess / foaf.knows / foaf.name

    name.head.pointer should be(Literal("Henry Story"))

  }

  "we must be able to project nodes to Scala types" in {

    (betehess / foaf.age).as[Int] should be(Success(29))

    (betehess / foaf.knows / foaf.name).as[String] should be(Success("Henry Story"))

  }

  "betehess should have three predicates: foaf:name foaf:age foaf:knows" in {

    val predicates = betehess.predicates.toList
    List(foaf.name, foaf.age, foaf.knows) foreach { p => predicates should contain(p) }

  }

  "we must be able to get rdf lists" in {

    (betehess / foaf("foo")).as[List[Int]] should be(Success(List(1, 2, 3)))

  }

  "we must be able to optionally get objects" in {

    (betehess / foaf.age).asOption[Int] should be(Success(Some(29)))

    (betehess / foaf.age).asOption[String] should be('failure)

    (betehess / foaf("unknown")).asOption[Int] should be(Success(None))

  }

  "asking for one (or exactly one) node when there is none must fail" in {

    (betehess / foaf("unknown")).takeOnePointedGraph should be('failure)

    (betehess / foaf("unknown")).exactlyOnePointedGraph should be('failure)

  }

  "asking for exactly one node when there are more than one must fail" in {

    (betehess / foaf.name).exactlyOnePointedGraph should be('failure)

  }

  "asking for one node when there is at least one must be a success" in {

    (betehess / foaf.name).takeOnePointedGraph should be('success)

    (betehess / foaf.age).takeOnePointedGraph should be('success)

  }

  "asking for exactly one pointed graph when there is none must fail" in {

    (betehess / foaf("unknown")).exactlyOnePointedGraph should be('failure)

  }

  "asking for exactly one pointed graph when there are more than one must fail" in {

    (betehess / foaf.name).exactlyOnePointedGraph should be('failure)

  }

  "getAllInstancesOf must give all instances of a given class" in {

    val persons = betehess.graph.getAllInstancesOf(foaf.Person).nodes

    persons.toSet should be(Set(URI("http://bertails.org/#betehess"), URI("http://bblfish.net/#hjs")))

  }

  "isA must test if a node belongs to a class" in {

    betehess.isA(foaf.Person) should be(true)

    betehess.isA(foaf("SomethingElse")) should be(false)

  }

}

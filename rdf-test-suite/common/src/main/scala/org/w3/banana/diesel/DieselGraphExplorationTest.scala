package org.w3.banana.diesel

import org.w3.banana._
import org.w3.banana.syntax._
import com.inthenow.zcheck.SpecLite
import scala.util._

class DieselGraphExplorationTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

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
    names.map(_.pointer).toSet must_==(Set(Literal.tagged("Alexandre", Lang("fr")), Literal.tagged("Alexander", Lang("en"))))
  }

  "'/' method must work with uris and bnodes" in {

    val name = betehess / foaf.knows / foaf.name

    name.head.pointer must_==(Literal("Henry Story"))

  }

  "we must be able to project nodes to Scala types" in {

    (betehess / foaf.age).as[Int] must_==(Success(29))

    (betehess / foaf.knows / foaf.name).as[String] must_==(Success("Henry Story"))

  }

  "betehess should have three predicates: foaf:name foaf:age foaf:knows" in {

    val predicates = betehess.predicates.toList
    List(foaf.name, foaf.age, foaf.knows) foreach { p => check(predicates.contains(p)) }

  }

  "we must be able to get rdf lists" in {

    (betehess / foaf("foo")).as[List[Int]] must_==(Success(List(1, 2, 3)))

  }

  "we must be able to optionally get objects" in {

    (betehess / foaf.age).asOption[Int] must_==(Success(Some(29)))

    check((betehess / foaf.age).asOption[String].isFailure)

    (betehess / foaf("unknown")).asOption[Int] must_==(Success(None))

  }

  "asking for one (or exactly one) node when there is none must fail" in {

    check((betehess / foaf("unknown")).takeOnePointedGraph.isFailure)

    check((betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure)

  }

  "asking for exactly one node when there are more than one must fail" in {

    check((betehess / foaf.name).exactlyOnePointedGraph.isFailure)

  }

  "asking for one node when there is at least one must be a success" in {

    check((betehess / foaf.name).takeOnePointedGraph.isSuccess)

    check((betehess / foaf.age).takeOnePointedGraph.isSuccess)

  }

  "asking for exactly one pointed graph when there is none must fail" in {

    check((betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure)

  }

  "asking for exactly one pointed graph when there are more than one must fail" in {

    check((betehess / foaf.name).exactlyOnePointedGraph.isFailure)

  }

  "getAllInstancesOf must give all instances of a given class" in {

    val persons = betehess.graph.getAllInstancesOf(foaf.Person).nodes

    persons.toSet must_==(Set(URI("http://bertails.org/#betehess"), URI("http://bblfish.net/#hjs")))

  }

  "isA must test if a node belongs to a class" in {

    check(betehess.isA(foaf.Person))

    check(! betehess.isA(foaf("SomethingElse")))

  }

}

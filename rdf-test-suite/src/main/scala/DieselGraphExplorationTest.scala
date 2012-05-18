package org.w3.banana.diesel

import org.w3.banana._

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz._
import scalaz.Validation._

abstract class DieselGraphExplorationTest[Rdf <: RDF](
  val ops: RDFOperations[Rdf],
  val dsl: Diesel[Rdf],
  val iso: GraphIsomorphism[Rdf]) extends WordSpec with MustMatchers {

  import org.scalatest.matchers.{BeMatcher, MatchResult}
  import ops._
  import dsl._
  import iso._

  val rdf = RDFPrefix(ops)
  val foaf = FOAFPrefix(ops)
  val xsd = XSDPrefix(ops)

  val betehess: PointedGraph[Rdf] = (
    uri("http://bertails.org/#betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.age ->- 29
    -- foaf.knows ->- (
      uri("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- uri("http://webid.info/")
    )
  )


  "'/' method must traverse the graph" in {

    val name = betehess / foaf.name

    name.head.node must be (LangLiteral("Alexandre", Lang("fr")))

  }

  "'/' method must work with uris and bnodes" in {

    val name = betehess / foaf.knows / foaf.name

    name.head.node must be (TypedLiteral("Henry Story"))

  }

  "we must be able to project nodes to Scala types" in {

    (betehess / foaf.age).exactlyOne.flatMap(_.asInt) must be (Success(29))

    (betehess / foaf.knows / foaf.name).takeOne.flatMap(_.asString) must be (Success("Henry Story"))

  }

  "betehess should have three predicates: foaf:name foaf:age foaf:knows" in {

    val predicates = betehess.predicates.toList
    List(foaf.name, foaf.age, foaf.knows) foreach { p => predicates must contain (p) }

  }

}

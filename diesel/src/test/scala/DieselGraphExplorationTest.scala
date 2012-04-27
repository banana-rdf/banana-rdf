package org.w3.rdf.diesel

import org.w3.rdf._

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz.Validation
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

  val betehess: GraphNode = (
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

    val name = betehess / foaf.knows flatMap { _ / foaf.name }

    name.head.node must be (TypedLiteral("Henry Story"))

  }


}


import org.w3.rdf.jena._

class JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)


import org.w3.rdf.sesame._

class SesameDieselGraphExplorationTest extends DieselGraphExplorationTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)

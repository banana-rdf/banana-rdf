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

  object FOAF extends Prefix {
    def apply(s: String): IRI = prefixBuilder("http://xmlns.com/foaf/0.1/")(s)
    val name = apply("name")
    val title = apply("title")
    val knows = apply("knows")
    val currentProject = apply("currentProject")
    val Person = apply("Person")
    val age = apply("age")
    val height = apply("height")
  }

  val betehess: GraphNode = (
    uri("http://bertails.org/#betehess")
    -- FOAF.name ->- "Alexandre".lang("fr")
    -- FOAF.age ->- 29
    -- FOAF.knows ->- (
      uri("http://bblfish.net/#hjs")
      -- FOAF.name ->- "Henry Story"
      -- FOAF.currentProject ->- uri("http://webid.info/")
    )
  )


  "'/' method must traverse the graph" in {

    val name = betehess / FOAF.name

    name.head.node must be (LangLiteral("Alexandre", Lang("fr")))

  }

  "'/' method must work with uris and bnodes" in {

    val name = betehess / FOAF.knows flatMap { _ / FOAF.name }

    name.head.node must be (TypedLiteral("Henry Story"))

  }


}


import org.w3.rdf.jena._

class JenaDieselGraphExplorationTest extends DieselGraphExplorationTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)


import org.w3.rdf.sesame._

class SesameDieselGraphExplorationTest extends DieselGraphExplorationTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)

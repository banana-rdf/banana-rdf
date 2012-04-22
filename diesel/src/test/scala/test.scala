package org.w3.rdf.diesel

import org.w3.rdf._

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz.Validation
import scalaz.Validation._

abstract class DieselTest[Rdf <: RDF](
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
  }

  "Diesel must construct a simple GraphNode" in {

    val g: GraphNode =
      bnode("betehess") --
        FOAF.name --> "Alexandre".lang("fr") --
        FOAF.title --> "Mr"

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), FOAF.title, TypedLiteral("Mr")))

    assert(g.graph isIsomorphicWith expectedGraph)
    

  }

}


import org.w3.rdf.jena._

object JenaDiesel extends Diesel(JenaOperations, JenaGraphUnion)

class JenaTest extends DieselTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)

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
    val knows = apply("knows")
    val currentProject = apply("currentProject")
    val Person = apply("Person")
  }

  "Diesel must accept a GraphNode in the object position" in {

    val g: GraphNode = (
      bnode("betehess")
        -- FOAF.name ->- "Alexandre".lang("fr")
        -- FOAF.title ->- "Mr"
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), FOAF.title, TypedLiteral("Mr")))

    assert(g.graph isIsomorphicWith expectedGraph)
    

  }




  "Diesel must construct a simple GraphNode" in {

    val g: GraphNode = (
      bnode("betehess")
        -- FOAF.name ->- "Alexandre".lang("fr")
        -- FOAF.knows ->- (
          uri("http://bblfish.net/#hjs")
            -- FOAF.name ->- "Henry Story"
            -- FOAF.currentProject ->- uri("http://webid.info/")
        )
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), FOAF.knows, uri("http://bblfish.net/#hjs")),
        Triple(uri("http://bblfish.net/#hjs"), FOAF.name, TypedLiteral("Henry Story")),
        Triple(uri("http://bblfish.net/#hjs"), FOAF.currentProject, uri("http://webid.info/")))

    assert(g.graph isIsomorphicWith expectedGraph)
  }



  "Diesel must accept triples written in the inverse order o-p-s using <--" in {

    val g: GraphNode = (
      bnode("betehess")
        -- FOAF.name ->- "Alexandre".lang("fr")
        -<- FOAF.knows -- (
          uri("http://bblfish.net/#hjs") -- FOAF.name ->- "Henry Story"
        )
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(IRI("http://bblfish.net/#hjs"), FOAF.knows, BNode("betehess")),
        Triple(IRI("http://bblfish.net/#hjs"), FOAF.name, TypedLiteral("Henry Story")))

    assert(g.graph isIsomorphicWith expectedGraph)
  }


  "Diesel must allow easy definition of class a..." in {

    val g: GraphNode = (
      bnode("betehess").a(FOAF.Person)
        -- FOAF.name ->- "Alexandre".lang("fr")
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), rdf("type"), FOAF.Person),
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexandre", Lang("fr"))))

    assert(g.graph isIsomorphicWith expectedGraph)
  }



}


import org.w3.rdf.jena._

object JenaDiesel extends Diesel(JenaOperations, JenaGraphUnion)

class JenaTest extends DieselTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)



import org.w3.rdf.sesame._

object SesameDiesel extends Diesel(SesameOperations, SesameGraphUnion)

class SesameTest extends DieselTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)

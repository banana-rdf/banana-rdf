package org.w3.rdf.diesel

import org.w3.rdf._

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz.Validation
import scalaz.Validation._

abstract class DieselGraphConstructTest[Rdf <: RDF](
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


  "Diesel must allow easy use of rdf:type through the method 'a'" in {

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



  "Diesel must allow objectList definition" in {

    val g: GraphNode =
      bnode("betehess") -- FOAF.name ->- ("Alexandre".lang("fr"), "Alexander".lang("en"))

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), FOAF.name, LangLiteral("Alexander", Lang("en"))))

    assert(g.graph isIsomorphicWith expectedGraph)
  }


  "Diesel must understand Scala's native types" in {

    val g: GraphNode = (
      bnode("betehess")
        -- FOAF.name ->- "Alexandre"
        -- FOAF.age ->- 29
        -- FOAF.height ->- 1.80
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), FOAF.name, TypedLiteral("Alexandre", xsdString)),
        Triple(BNode("betehess"), FOAF.age, TypedLiteral("29", xsdInt)),
        Triple(BNode("betehess"), FOAF.height, TypedLiteral("1.8", xsdDouble)))

    assert(g.graph isIsomorphicWith expectedGraph)
  }

  "Diesel must support RDF collections" in {

    val g: GraphNode = (
      bnode("betehess")
        -- FOAF.name ->- List[Node](29, bnode("bar"), "foo")
    )


    val l: GraphNode = (
      bnode()
        -- first ->- 29
        -- rest ->- (
          bnode()
            -- first ->- bnode("bar")
            -- rest ->- (
              bnode()
                -- first ->- "foo"
                -- rest ->- nil
            )
        )
    )

    val expectedGraph = (
      bnode("betehess") -- FOAF.name ->- l
    )

    assert(g.graph isIsomorphicWith expectedGraph.graph)
  }




  "Diesel must support RDF collections (empty list)" in {

    val g: GraphNode = (
      bnode("betehess") -- FOAF.name ->- List[Node]()
    )

    val expectedGraph = (
      bnode("betehess") -- FOAF.name ->- nil
    )

    assert(g.graph isIsomorphicWith expectedGraph.graph)
  }



}


import org.w3.rdf.jena._

class JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)


import org.w3.rdf.sesame._

class SesameDieselGraphConstructTest extends DieselGraphConstructTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)

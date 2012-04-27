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

  val rdf = RDFPrefix(ops)

  val foaf = FOAFPrefix(ops)
  val xsd = XSDPrefix(ops)

  "Diesel must accept a GraphNode in the object position" in {

    val g: GraphNode = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre".lang("fr")
        -- foaf.title ->- "Mr"
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), foaf.title, TypedLiteral("Mr")))

    assert(g.graph isIsomorphicWith expectedGraph)
    

  }




  "Diesel must construct a simple GraphNode" in {

    val g: GraphNode = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre".lang("fr")
        -- foaf.knows ->- (
          uri("http://bblfish.net/#hjs")
            -- foaf.name ->- "Henry Story"
            -- foaf.currentProject ->- uri("http://webid.info/")
        )
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), foaf.knows, uri("http://bblfish.net/#hjs")),
        Triple(uri("http://bblfish.net/#hjs"), foaf.name, TypedLiteral("Henry Story")),
        Triple(uri("http://bblfish.net/#hjs"), foaf.currentProject, uri("http://webid.info/")))

    assert(g.graph isIsomorphicWith expectedGraph)
  }



  "Diesel must accept triples written in the inverse order o-p-s using <--" in {

    val g: GraphNode = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre".lang("fr")
        -<- foaf.knows -- (
          uri("http://bblfish.net/#hjs") -- foaf.name ->- "Henry Story"
        )
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(IRI("http://bblfish.net/#hjs"), foaf.knows, BNode("betehess")),
        Triple(IRI("http://bblfish.net/#hjs"), foaf.name, TypedLiteral("Henry Story")))

    assert(g.graph isIsomorphicWith expectedGraph)
  }


  "Diesel must allow easy use of rdf:type through the method 'a'" in {

    val g: GraphNode = (
      bnode("betehess").a(foaf.Person)
        -- foaf.name ->- "Alexandre".lang("fr")
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), rdf("type"), foaf.Person),
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))))

    assert(g.graph isIsomorphicWith expectedGraph)
  }



  "Diesel must allow objectList definition" in {

    val g: GraphNode =
      bnode("betehess") -- foaf.name ->- ("Alexandre".lang("fr"), "Alexander".lang("en"))

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexander", Lang("en"))))

    assert(g.graph isIsomorphicWith expectedGraph)
  }


  "Diesel must understand Scala's native types" in {

    val g: GraphNode = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre"
        -- foaf.age ->- 29
        -- foaf.height ->- 1.80
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, TypedLiteral("Alexandre", xsd.string)),
        Triple(BNode("betehess"), foaf.age, TypedLiteral("29", xsd.int)),
        Triple(BNode("betehess"), foaf.height, TypedLiteral("1.8", xsd.double)))

    assert(g.graph isIsomorphicWith expectedGraph)
  }

  "Diesel must support RDF collections" in {

    val g: GraphNode = (
      bnode("betehess")
        -- foaf.name ->- List[Node](29, bnode("bar"), "foo")
    )


    val l: GraphNode = (
      bnode()
        -- rdf.first ->- 29
        -- rdf.rest ->- (
          bnode()
            -- rdf.first ->- bnode("bar")
            -- rdf.rest ->- (
              bnode()
                -- rdf.first ->- "foo"
                -- rdf.rest ->- rdf.nil
            )
        )
    )

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- l
    )

    assert(g.graph isIsomorphicWith expectedGraph.graph)
  }




  "Diesel must support RDF collections (empty list)" in {

    val g: GraphNode = (
      bnode("betehess") -- foaf.name ->- List[Node]()
    )

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- rdf.nil
    )

    assert(g.graph isIsomorphicWith expectedGraph.graph)
  }



}


import org.w3.rdf.jena._

class JenaDieselGraphConstructTest extends DieselGraphConstructTest[Jena](JenaOperations, JenaDiesel, JenaGraphIsomorphism)


import org.w3.rdf.sesame._

class SesameDieselGraphConstructTest extends DieselGraphConstructTest[Sesame](SesameOperations, SesameDiesel, SesameGraphIsomorphism)

package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation._

abstract class DieselGraphConstructTest[Rdf <: RDF](
  val diesel: Diesel[Rdf],
  val iso: GraphIsomorphism[Rdf]) extends WordSpec with MustMatchers {

  import org.scalatest.matchers.{BeMatcher, MatchResult}
  import diesel._
  import ops._
  import iso._

  val foaf = FOAFPrefix(ops)

  "Diesel must accept a GraphNode in the object position" in {

    val g: PointedGraph[Rdf] = (
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

    val g: PointedGraph[Rdf] = (
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

    val g: PointedGraph[Rdf] = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre".lang("fr")
        -<- foaf.knows -- (
          uri("http://bblfish.net/#hjs") -- foaf.name ->- "Henry Story"
        )
    )

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(URI("http://bblfish.net/#hjs"), foaf.knows, BNode("betehess")),
        Triple(URI("http://bblfish.net/#hjs"), foaf.name, TypedLiteral("Henry Story")))

    assert(g.graph isIsomorphicWith expectedGraph)
  }


  "Diesel must allow easy use of rdf:type through the method 'a'" in {

    val g: PointedGraph[Rdf] = (
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

    val g: PointedGraph[Rdf] =
      bnode("betehess") -- foaf.name ->- ("Alexandre".lang("fr"), "Alexander".lang("en"))

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexandre", Lang("fr"))),
        Triple(BNode("betehess"), foaf.name, LangLiteral("Alexander", Lang("en"))))

    assert(g.graph isIsomorphicWith expectedGraph)
  }


  "Diesel must understand Scala's native types" in {

    val g = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre"
        -- foaf.age ->- 29
        -- foaf.height ->- 1.80
    ).graph

    val expectedGraph =
      Graph(
        Triple(BNode("betehess"), foaf.name, TypedLiteral("Alexandre", xsd.string)),
        Triple(BNode("betehess"), foaf.age, TypedLiteral("29", xsd.int)),
        Triple(BNode("betehess"), foaf.height, TypedLiteral("1.8", xsd.double)))

    assert(g isIsomorphicWith expectedGraph)
  }

  "Diesel must support RDF collections" in {

    val g: PointedGraph[Rdf] = (
      bnode("betehess")
        -- foaf.name -->- List(1, 2, 3)
    )


    val l: PointedGraph[Rdf] = (
      bnode()
        -- rdf.first ->- 1
        -- rdf.rest ->- (
          bnode()
            -- rdf.first ->- 2
            -- rdf.rest ->- (
              bnode()
                -- rdf.first ->- 3
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

    val g: PointedGraph[Rdf] = (
      bnode("betehess") -- foaf.name -->- List[String]()
    )

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- rdf.nil
    )

    assert(g.graph isIsomorphicWith expectedGraph.graph)
  }


  "providing a None as an object does not emit a triple" in {

    val g = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre"
        -- foaf.age ->- none[Int]
    ).graph

    val expectedGraph = (
      bnode("betehess") -- foaf.name ->- "Alexandre"
    ).graph

    assert(g isIsomorphicWith expectedGraph)

  }



  "providing a Some(t) as an object just emits the triple with t as an object" in {

    val g = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre"
        -- foaf.age ->- some(42)
    ).graph

    val expectedGraph = (
      bnode("betehess")
        -- foaf.name ->- "Alexandre"
        -- foaf.age ->- 42
    ).graph

    assert(g isIsomorphicWith expectedGraph)

  }



}

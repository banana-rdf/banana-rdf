/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana.diesel

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.w3.banana.*
import org.w3.banana.diesel.{given, *}
//import org.w3.banana.syntax._
//import scalaz.Scalaz.{none, some}
import scala.language.implicitConversions

/** test to build relative graphs using our DSL. Building relative graphs should be more common.
  * Graphs with full URLs are more likely downloaded from the web. TODO: build a DSL for
  * non-relative graphs.
  */
open class DieselRelativeGraphConstructTest[Rdf <: RDF](using ops: Ops[Rdf])
    extends AnyWordSpec with Matchers:

   import ops.{given, *}
   val TodoLater = org.scalatest.Tag("TodoLater")

   val foaf = prefix.FOAF[Rdf]
   val fr   = Lang("fr") // todo, put together a list of Lang constants
   val en   = Lang("en")

   "Diesel must accept a GraphNode in the object position" in {
     val g: PointedRGraph[Rdf] =
       (
         (rURI("#b") -- foaf.name ->- "Alexandre".lang(fr)) -- foaf.title ->- "Mr"
       )

     val expectedGraph =
       rGraph(
         rTriple(rURI("#b"), foaf.name, Literal("Alexandre", fr)),
         rTriple(rURI("#b"), foaf.title, Literal("Mr"))
       )

     (g.graph isomorphic expectedGraph) shouldEqual true
   }

   "Diesel must construct a simple Graph" in {

     val g: PointedRGraph[Rdf] =
       (
         (BNode("betehess") -- foaf.name ->- "Alexandre".lang(fr)) -- foaf.knows ->- (
           (rURI(
             "http://bblfish.net/#hjs"
           ) -- foaf.name ->- "Henry Story") -- foaf.currentProject ->- rURI("http://webid.info/")
         )
       )

     val expectedGraph: RDF.rGraph[Rdf] =
       Graph(
         Triple(BNode("betehess"), foaf.name, Literal("Alexandre", Lang("fr"))),
         Triple(BNode("betehess"), foaf.knows, URI("http://bblfish.net/#hjs")),
         Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story")),
         Triple(URI("http://bblfish.net/#hjs"), foaf.currentProject, URI("http://webid.info/"))
       )

     (g.graph isomorphic expectedGraph) shouldEqual true
   }

   "Diesel must accept triples written in the inverse order o-p-s using <--" in {
     val uriSubjPg: PointedRGraph[Rdf] =
       rURI("#betehess") -<- foaf.knows -- rURI("https://bblfish.net/people/henry/card#me")
     assert(uriSubjPg.graph.triples.size == 1)
     assert(uriSubjPg.graph.triples.head == rTriple(
       rURI("https://bblfish.net/people/henry/card#me"),
       foaf.knows,
       rURI("#betehess")
     ))
     assert(uriSubjPg.pointer == rURI("#betehess"))

     val bnodeSubjPg: PointedRGraph[Rdf] =
       BNode("betehess") -<- foaf.knows -- rURI("https://bblfish.net/people/henry/card#me")
     assert(bnodeSubjPg.graph.triples.size == 1)
     assert(bnodeSubjPg.graph.triples.head == rTriple(
       rURI("https://bblfish.net/people/henry/card#me"),
       foaf.knows,
       BNode("betehess")
     ))
     assert(bnodeSubjPg.pointer == BNode("betehess"))

     val litSubjPg: PointedRGraph[Rdf] =
       Literal("Henry") -<- foaf.name -- rURI("https://bblfish.net/#hjs")
     assert(litSubjPg.graph.triples.size == 1)
     assert(litSubjPg.graph.triples.head == rTriple(
       rURI("https://bblfish.net/#hjs"),
       foaf.name,
       Literal("Henry")
     ))
     assert(litSubjPg.pointer == Literal("Henry"))

     val g: PointedRGraph[Rdf] = (
       BNode("betehess")
         -- foaf.name ->- "Alexandre".lang(fr)
         -<- foaf.knows -- rURI("/#hjs")
     )
     assert(g.pointer == BNode("betehess"))
     val expectedGr: RDF.rGraph[Rdf] =
       rGraph(
         rTriple(BNode("betehess"), foaf.name, Literal("Alexandre", Lang("fr"))),
         rTriple(rURI("/#hjs"), foaf.knows, BNode("betehess"))
       )
     (g.graph isomorphic expectedGr) shouldEqual true

     val henryNamePG: PointedSubjRGraph[Rdf] = rURI("#hjs") -- foaf.name ->- "Henry Story"
     val henryExpectedGr = rGraph(
       rTriple(rURI("#hjs"), foaf.name, Literal("Henry Story"))
     )
     assert(henryNamePG.pointer == rURI("#hjs"))
     assert(henryNamePG.graph isomorphic henryExpectedGr)

     val g2: PointedRGraph[Rdf] = (
       BNode("betehess")
         -- foaf.name ->- "Alexandre".lang(fr)
         -<- foaf.knows -- henryNamePG
     )

     val expectedGraph2: RDF.rGraph[Rdf] =
       rGraph(
         rTriple(BNode("betehess"), foaf.name, Literal("Alexandre", Lang("fr"))),
         rTriple(rURI("#hjs"), foaf.knows, BNode("betehess")),
         rTriple(rURI("#hjs"), foaf.name, Literal("Henry Story"))
       )

     (g2.graph isomorphic expectedGraph2) shouldEqual true
   }

   "Diesel must allow easy use of rdf:type through the method 'a'" in {
     val g: PointedRGraph[Rdf] = (
       rURI("#betehess").a(foaf.Person)
         -- foaf.name ->- "Alexandre".lang(fr)
     )

     val expectedGraph =
       rGraph(
         rTriple(rURI("#betehess"), rdf.`type`, foaf.Person),
         rTriple(rURI("#betehess"), foaf.name, Literal("Alexandre", Lang("fr")))
       )

     (g.graph isomorphic expectedGraph) shouldEqual true
   }

   "Diesel must allow objectList definition with simple syntax".taggedAs(TodoLater) ignore {
//     val g: PointedRelGraph[Rdf] =
//       rURI("#betehess") -- foaf.name ->- ("Alexandre".lang(fr), "Alexander".lang(en))
//
//     val expectedGraph =
//       rGraph(
//         rTriple(rURI("#betehess"), foaf.name, Literal("Alexandre", Lang("fr"))),
//         rTriple(rURI("#betehess"), foaf.name, Literal("Alexander", Lang("en")))
//       )
//
//     (g.graph isomorphic expectedGraph) shouldEqual true
   }

   "Diesel must allow explicit objectList definition".taggedAs(TodoLater) ignore {
//     val alexs = Seq(
//       rURI("a") -- foaf.name ->- "Alexandre".lang(fr),
//       rURI("b") -- foaf.name ->- "Alexander".lang(en)
//     )
//
//     val g = (
//       URI("http://bblfish.net/#hjs")
//         -- foaf.name ->- "Henry Story"
//         -- foaf.knows ->- ObjectList(alexs)
//     )
//
//     val expectedGraph =
//       Graph(
//         Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story")),
//         Triple(URI("http://bblfish.net/#hjs"), foaf.knows, BNode("a")),
//         Triple(URI("http://bblfish.net/#hjs"), foaf.knows, BNode("b")),
//         Triple(BNode("a"), foaf.name, Literal("Alexander", Lang("en"))),
//         Triple(BNode("b"), foaf.name, Literal("Alexandre", Lang("fr")))
//       )
//
//     (g.graph isomorphic expectedGraph) shouldEqual true
   }

   "Diesel with empty explicit objectList definition".taggedAs(TodoLater) ignore {
//     val g =
//       (
//         (URI("http://bblfish.net/#hjs") -- foaf.name ->- "Henry Story")
//           -- foaf.knows ->- ObjectList(Seq.empty[Int])
//       )
//
//     val expectedGraph = Graph(
//       Triple(URI("http://bblfish.net/#hjs"), foaf.name, Literal("Henry Story"))
//     )
//
//     (g.graph isomorphic expectedGraph) shouldEqual true
   }

   "Diesel must understand Scala's native types".taggedAs(TodoLater) ignore {
//
//     val g = (
//       BNode("betehess")
//         -- foaf.name ->- "Alexandre"
//         -- foaf.age ->- 29
//         -- foaf.height ->- 1.80
//     ).graph
//
//     val expectedGraph =
//       Graph(
//         Triple(BNode("betehess"), foaf.name, Literal("Alexandre", xsd.string)),
//         Triple(BNode("betehess"), foaf.age, Literal("29", xsd.integer)),
//         Triple(BNode("betehess"), foaf.height, Literal("1.8", xsd.double))
//       )
//
//     (g isomorphic expectedGraph)
   }

   "Diesel must support RDF collections".taggedAs(TodoLater) ignore {

//     val g: PointedRelGraph[Rdf] = (
//       BNode("betehess") -- foaf.name ->- List(1, 2, 3)
//     )
//
//     val l: PointedGraph[Rdf] =
//       (
//         BNode() -- rdf.first ->- 1
//           -- rdf.rest ->- (
//             bnode()
//               -- rdf.first ->- 2
//               -- rdf.rest ->- (
//                 bnode()
//                   -- rdf.first ->- 3
//                   -- rdf.rest ->- rdf.nil
//               )
//           )
//       )
//
//     val expectedGraph = (
//       bnode("betehess") -- foaf.name ->- l
//     )
//
//     (g.graph isIsomorphicWith expectedGraph.graph) shouldEqual true
   }

   "Diesel must support RDF collections (empty list)".taggedAs(TodoLater) ignore {

//     val g: PointedGraph[Rdf] = (
//       bnode("betehess") -- foaf.name ->- List[String]()
//     )
//
//     val expectedGraph = (
//       bnode("betehess") -- foaf.name ->- rdf.nil
//     )
//
//     (g.graph isIsomorphicWith expectedGraph.graph) shouldEqual true
   }

   "providing a None as an object does not emit a triple".taggedAs(TodoLater) ignore {
//     val g = (
//       bnode("betehess")
//         -- foaf.name ->- "Alexandre"
//         -- foaf.age ->- none[Int]
//     ).graph
//
//     val expectedGraph = (
//       bnode("betehess") -- foaf.name ->- "Alexandre"
//     ).graph
//
//     (g isIsomorphicWith expectedGraph) shouldEqual true

   }

//  "test JSON Literals" in {
//    val jwtRsa = """{
//						|   "kty" : "RSA",
//						|   "n"   : "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx
//						|            4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs
//						|            tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2
//						|            QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI
//						|            SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb
//						|            w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
//						|   "e"   : "AQAB",
//						|   "alg" : "RS256",
//						|   "kid" : "2011-04-29"
//						| }""".stripMargin
//
//    val cert = CertPrefix[Rdf]
//    val keyId = URI("http://alice.example/key#i")
//    val jwtPg = keyId -- cert.key ->- Literal(jwtRsa, rdf.JSON)
//
//    val expectedGraph =
//      Graph(
//        Triple(keyId, cert.key, Literal(jwtRsa, rdf.JSON)),
//      )
//
//    (jwtPg.graph isIsomorphicWith expectedGraph) shouldEqual true
//  }
//
//  "providing a Some(t) as an object just emits the triple with t as an object" in {
//
//    val g = (
//      bnode("betehess")
//      -- foaf.name ->- "Alexandre"
//      -- foaf.age ->- some(42)
//    ).graph
//
//    val expectedGraph = (
//      bnode("b")
//      -- foaf.name ->- "Alexandre"
//      -- foaf.age ->- 42
//    ).graph
//
//    (g isIsomorphicWith expectedGraph) shouldEqual true
//
//  }
//
//  "disconnected graph construction" in {
//
//    val g = (
//      bnode("a") -- foaf.name ->- "Alexandre"
//      -- foaf.age ->- 29
//    ).graph union (
//        bnode("h") -- foaf.name ->- "Henry"
//        -- foaf.height ->- 1.92
//      ).graph
//
//    val expectedGraph =
//      Graph(
//        Triple(bnode("a"), foaf.name, Literal("Alexandre", xsd.string)),
//        Triple(bnode("a"), foaf.age, Literal("29", xsd.integer)),
//        Triple(bnode("h"), foaf.name, Literal("Henry", xsd.string)),
//        Triple(bnode("h"), foaf.height, Literal("1.92", xsd.double))
//      )
//
//    (g.graph isIsomorphicWith expectedGraph) shouldEqual true
//
//  }
//
//  "Diesel must support sets" in {
//
//    val pg: PointedGraph[Rdf] = (
//      bnode("betehess") -- foaf.name ->- Set(
//        1.toPG,
//        "blah".toPG,
//        bnode("foo") -- foaf.homepage ->- URI("http://example.com")
//      )
//    )
//
//    val expectedGraph = Graph(Set(
//      Triple(bnode("betehess"), foaf.name, Literal("1", xsd.integer)),
//      Triple(bnode("betehess"), foaf.name, Literal("blah")),
//      Triple(bnode("betehess"), foaf.name, bnode("foo")),
//      Triple(bnode("foo"), foaf.homepage, URI("http://example.com"))
//    ))
//
//    (pg.graph isIsomorphicWith expectedGraph) shouldEqual true
//  }

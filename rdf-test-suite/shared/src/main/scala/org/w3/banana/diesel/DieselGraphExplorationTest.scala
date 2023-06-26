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

//package org.w3.banana.diesel
//
//import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.matchers.should.Matchers
//
//import org.w3.banana._
//
//import scala.util._
//
//class DieselGraphExplorationTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends AnyWordSpec with Matchers {
//
//  import ops._
//
//  val foaf = FOAFPrefix[Rdf]
//  val alex = URI("http://bertails.org/#betehess")
//  val henry = URI("http://bblfish.net/#hjs")
//
//  val betehess: PointedGraph[Rdf] = (
//    alex.a(foaf.Person)
//    -- foaf.name ->- "Alexandre".lang("fr")
//    -- foaf.name ->- "Alexander".lang("en")
//    -- foaf.age ->- 29
//    -- foaf("foo") ->- List(1, 2, 3)
//    -- foaf.knows ->- (
//      henry.a(foaf.Person)
//      -- foaf.name ->- "Henry Story"
//      -- foaf.currentProject ->- URI("http://webid.info/")
//      -- foaf.knows ->- (BNode("t").a(foaf.Person) -- foaf.name ->- "Tim")
//    )
//  )
//
//  "'/' method must traverse the graph" in {
//    val names = betehess / foaf.name
//    names.map(_.pointer).toSet shouldEqual Set(
//      Literal.tagged("Alexandre", Lang("fr")),
//      Literal.tagged("Alexander", Lang("en"))
//    )
//  }
//
//  "'/' method must work with uris and bnodes" in {
//
//    val name = betehess / foaf.knows / foaf.name
//
//    name.head.pointer shouldEqual Literal("Henry Story")
//
//  }
//
//  "'/-' backward search method" in {
//    val ppl: PointedGraphs[Rdf] = PointedGraph(foaf.Person,betehess.graph)/-rdf.`type`
//    ppl.nodes.toSet shouldEqual Set[Rdf#Node](alex,henry,BNode("t"))
//    val names: Set[Rdf#Node] = (ppl/foaf.name).nodes.toSet
//    names shouldEqual  Set[Rdf#Node](
//      Literal.tagged("Alexandre", Lang("fr")),
//      Literal.tagged("Alexander", Lang("en")),
//      Literal("Henry Story"),
//      Literal("Tim")
//    )
//    //also search backward on PointedGraphs
//    (ppl/-rdf.`type`).nodes.toSet shouldEqual(Set())
//    (ppl/-foaf.knows).nodes.toSet shouldEqual Set(henry,alex)
//  }
//
//  "we must be able to project nodes to Scala types" in {
//
//    (betehess / foaf.age).as[Int] shouldEqual Success(29)
//
//    (betehess / foaf.knows / foaf.name).as[String] shouldEqual Success("Henry Story")
//
//  }
//
//  "betehess should have three predicates: foaf:name foaf:age foaf:knows" in {
//
//    val predicates = betehess.predicates.toList
//    List(foaf.name, foaf.age, foaf.knows) foreach { p => predicates.contains(p) }
//
//  }
//
//  "we must be able to get rdf lists" in {
//
//    (betehess / foaf("foo")).as[List[Int]] shouldEqual Success(List(1, 2, 3))
//
//  }
//
//  "we must be able to optionally get objects" in {
//
//    (betehess / foaf.age).asOption[Int] shouldEqual Success(Some(29))
//
//    (betehess / foaf.age).asOption[String].isFailure  shouldEqual true
//
//    (betehess / foaf("unknown")).asOption[Int] shouldEqual Success(None)
//
//  }
//
//  "asking for one (or exactly one) node when there is none must fail" in {
//
//    (betehess / foaf("unknown")).takeOnePointedGraph.isFailure  shouldEqual true
//
//    (betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure  shouldEqual true
//
//  }
//
//  "asking for exactly one node when there are more than one must fail" in {
//
//    (betehess / foaf.name).exactlyOnePointedGraph.isFailure  shouldEqual true
//
//  }
//
//  "asking for one node when there is at least one must be a success" in {
//
//    (betehess / foaf.name).takeOnePointedGraph.isSuccess  shouldEqual true
//
//    (betehess / foaf.age).takeOnePointedGraph.isSuccess  shouldEqual true
//
//  }
//
//  "asking for exactly one pointed graph when there is none must fail" in {
//
//    (betehess / foaf("unknown")).exactlyOnePointedGraph.isFailure shouldEqual true
//
//  }
//
//  "asking for exactly one pointed graph when there are more than one must fail" in {
//
//    (betehess / foaf.name).exactlyOnePointedGraph.isFailure shouldEqual true
//
//  }
//
//  "getAllInstancesOf must give all instances of a given class" in {
//
//    val persons = betehess.graph.getAllInstancesOf(foaf.Person).nodes
//
//    persons.toSet shouldEqual Set(URI("http://bertails.org/#betehess"), URI("http://bblfish.net/#hjs"), BNode("t"))
//
//  }
//
//  "isA must test if a node belongs to a class" in {
//
//    betehess.isA(foaf.Person)  shouldEqual true
//
//    betehess.isA(foaf("SomethingElse"))  shouldEqual false
//
//  }
//
//}

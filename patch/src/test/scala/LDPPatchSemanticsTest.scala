package org.w3.banana

import org.w3.banana._
import org.scalatest._
import java.io._

abstract class LDPPatchSemanticsTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, Turtle]) extends WordSpec with Matchers {

  import ops._

  "join with common key ?x" in {
    val rs1 = ResultSet[Rdf](
      Set(Var("x"), Var("y"), Var("z")),
      Vector(
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar1")),
          (Var("z"), TypedLiteral("baz1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar2")),
          (Var("z"), TypedLiteral("baz2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo3")),
          (Var("y"), TypedLiteral("bar3")),
          (Var("z"), TypedLiteral("baz3"))
        ))
      ))
    val rs2 = ResultSet[Rdf](
      Set(Var("x"), Var("i")),
      Vector(
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("i"), TypedLiteral("2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo3")),
          (Var("i"), TypedLiteral("3"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo2")),
          (Var("i"), TypedLiteral("2719"))
        ))
      ))
    val expectedResultSet = ResultSet[Rdf](
      Set(Var("x"), Var("y"), Var("z"), Var("i")),
      Vector(
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar1")),
          (Var("z"), TypedLiteral("baz1")),
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar1")),
          (Var("z"), TypedLiteral("baz1")),
          (Var("i"), TypedLiteral("2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar2")),
          (Var("z"), TypedLiteral("baz2")),
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar2")),
          (Var("z"), TypedLiteral("baz2")),
          (Var("i"), TypedLiteral("2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo3")),
          (Var("y"), TypedLiteral("bar3")),
          (Var("z"), TypedLiteral("baz3")),
          (Var("i"), TypedLiteral("3"))
        ))
      ))

    (rs1 join rs2) should be (expectedResultSet)
  }

  "join with no common key" in {
    val rs1 = ResultSet[Rdf](
      Set(Var("x"), Var("y"), Var("z")),
      Vector(
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar1")),
          (Var("z"), TypedLiteral("baz1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar2")),
          (Var("z"), TypedLiteral("baz2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo3")),
          (Var("y"), TypedLiteral("bar3")),
          (Var("z"), TypedLiteral("baz3"))
        ))
      ))
    val rs2 = ResultSet[Rdf](
      Set(Var("i")),
      Vector(
        SolutionMapping[Rdf](Map(
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("i"), TypedLiteral("2"))
        ))
      ))
    val expectedResultSet = ResultSet[Rdf](
      Set(Var("x"), Var("y"), Var("z"), Var("i")),
      Vector(
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar1")),
          (Var("z"), TypedLiteral("baz1")),
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar1")),
          (Var("z"), TypedLiteral("baz1")),
          (Var("i"), TypedLiteral("2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar2")),
          (Var("z"), TypedLiteral("baz2")),
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo")),
          (Var("y"), TypedLiteral("bar2")),
          (Var("z"), TypedLiteral("baz2")),
          (Var("i"), TypedLiteral("2"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo3")),
          (Var("y"), TypedLiteral("bar3")),
          (Var("z"), TypedLiteral("baz3")),
          (Var("i"), TypedLiteral("1"))
        )),
        SolutionMapping[Rdf](Map(
          (Var("x"), TypedLiteral("foo3")),
          (Var("y"), TypedLiteral("bar3")),
          (Var("z"), TypedLiteral("baz3")),
          (Var("i"), TypedLiteral("2"))
        ))
      ))
    (rs1 join rs2) should be (expectedResultSet)
  }

  val graph: Rdf#Graph = reader.read("""
@prefix foaf: <http://xmlns.com/foaf/0.1/> .

_:betehess foaf:name "Alex" ;
  foaf:knows <http://bblfish.net/#hjs> .

<http://bblfish.net/#hjs> foaf:name "Henry Story" ;
  foaf:currentProject <http://webid.info/> .
""", "http://example.com").get

  "PATCH!" in {
    val patch = LDPPatchParser.parseOne[Rdf]("""
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
DELETE {
  ?s foaf:name "Alex"
}
INSERT {
  ?s foaf:name "Alexandre"
}
WHERE {
  ?s foaf:name "Alex"
}
""").get

  val expectedGraph: Rdf#Graph = reader.read("""
@prefix foaf: <http://xmlns.com/foaf/0.1/> .

[] foaf:name "Alexandre" ;
  foaf:knows <http://bblfish.net/#hjs> .

<http://bblfish.net/#hjs> foaf:name "Henry Story" ;
  foaf:currentProject <http://webid.info/> .
""", "http://example.com").get

    val patcher: LDPPatchCommand[Rdf] = new LDPPatchCommandImpl[Rdf]

    val patchedGraph = patcher.PATCH(graph, patch).get

    assert(patchedGraph isIsomorphicWith expectedGraph)

  }

  "PATCH2" in {
    val patch = LDPPatchParser.parseOne[Rdf]("""
DELETE {
  <http://bblfish.net/#hjs> ?p ?o
}
WHERE {
  <http://bblfish.net/#hjs> ?p ?o
}
""").get

  val expectedGraph: Rdf#Graph = reader.read("""
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
[] foaf:name "Alex" ;
  foaf:knows <http://bblfish.net/#hjs> .
""", "http://example.com").get

    val patcher: LDPPatchCommand[Rdf] = new LDPPatchCommandImpl[Rdf]

    val patchedGraph = patcher.PATCH(graph, patch).get

    assert(patchedGraph isIsomorphicWith expectedGraph)
  }

}

import org.w3.banana.jena._

class JenaPatchSemanticsTest extends LDPPatchSemanticsTest[Jena]

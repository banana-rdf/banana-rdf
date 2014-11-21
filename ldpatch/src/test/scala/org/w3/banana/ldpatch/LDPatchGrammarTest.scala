package org.w3.banana.ldpatch

import org.w3.banana._
import org.scalatest.{ Filter => _, _ }
import java.io._
import scala.util.{ Try, Success, Failure }
import org.w3.banana.ldpatch.model._

// TODO
import org.parboiled2._
import org.w3.banana.io._

abstract class LDPatchGrammarTest[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, Try, Turtle],
  writer: RDFWriter[Rdf, Try, Turtle]
) extends WordSpec with Matchers with TryValues { self =>

  import ops._

  /** transforms triples with vars into a regular RDF Graph (variables
    * are transformed into bnode)
    */
  def makeGraph(triples: Vector[model.Triple[Rdf]]): Rdf#Graph = {
    def makeNode(node: VarOrConcrete[Rdf]): Rdf#Node = node match {
      case Concrete(node) => node
      case Var(_) => BNode()
    }
    Graph(
      triples.map { case model.Triple(s, p, o) => Triple(makeNode(s), p, makeNode(o)) }
    )
  }


  val g = new Grammar[Rdf] { implicit val ops = self.ops }

  def newParser(input: String) =
    new g.grammar.PEGPatchParser(input, baseURI = URI("http://example.com/foo"), prefixes = Map("foaf" -> URI("http://xmlns.com/foaf/"), "schema" -> URI("http://schema.org/")))

  def newFreshParser(input: String) =
    new g.grammar.PEGPatchParser(input, baseURI = URI("http://example.com/foo"), prefixes = Map.empty)

  "parse IRIREF" in {
    newParser("""<http://example.com/foo#\u2665>""").IRIREF.run().success.value should be(URI("http://example.com/foo#♥"))
    newParser("""<#\u2665>""").IRIREF.run().success.value should be(URI("http://example.com/foo#♥"))
  }

  "parse iri" in {
    newParser("""<http://example.com/foo#\u2665>""").iri.run().success.value should be(URI("http://example.com/foo#♥"))
    newParser("""foaf:name""").iri.run().success.value should be(URI("http://xmlns.com/foaf/name"))
  }

  "parse prefixID" in {
    newParser("""@prefix example:<http://example.com/foo#> .""").prefixID.run().success.value should be("example" -> URI("http://example.com/foo#"))
    newParser("""@prefix   example: <http://example.com/foo#> .""").prefixID.run().success.value should be("example" -> URI("http://example.com/foo#"))
    newParser("""@prefix : <http://example.com/foo#> .""").prefixID.run().success.value should be("" -> URI("http://example.com/foo#"))
  }

  "parse BlankNode" in {
    // the only way to know about the expected bnode is to look into the map built by the parser
    val parser = newParser("""_:foo""")
    val parsedValue = parser.BlankNode.run().success.value
    parsedValue should be(parser.bnodeMap("foo"))
    newParser("""[]""").BlankNode.run() shouldBe a [Success[_]]
    newParser("""[ ]""").BlankNode.run() shouldBe a [Success[_]]
  }

  "parse RDFLiteral" in {
    newParser(""""foo"""").RDFLiteral.run().success.value should be(Literal("foo"))
    newParser(""""foo"@en""").RDFLiteral.run().success.value should be(Literal.tagged("foo", Lang("en")))
    newParser(""""foo"^^foaf:name""").RDFLiteral.run().success.value should be(Literal("foo", URI("http://xmlns.com/foaf/name")))
    newParser(""""foo"^^<http://xmlns.com/foaf/name>""").RDFLiteral.run().success.value should be(Literal("foo", URI("http://xmlns.com/foaf/name")))
  }

  "parse NumericLiteral" in {
    newParser("""42""").NumericLiteral.run().success.value should be(Literal("42", xsd.integer))
    newParser("""-42""").NumericLiteral.run().success.value should be(Literal("-42", xsd.integer))
    newParser("""3.14""").NumericLiteral.run().success.value should be(Literal("3.14", xsd.decimal))
    newParser("""-3.14""").NumericLiteral.run().success.value should be(Literal("-3.14", xsd.decimal))
    newParser("""42e-10""").NumericLiteral.run().success.value should be(Literal("42e-10", xsd.double))
    newParser("""-3.14E10""").NumericLiteral.run().success.value should be(Literal("-3.14E10", xsd.double))    
  }

  "parse BooleanLiteral" in {
    newParser("""true""").BooleanLiteral.run().success.value should be(xsd.`true`)
    newParser("""false""").BooleanLiteral.run().success.value should be(xsd.`false`)
  }

  "parse literal" in {
    newParser(""""foo"^^foaf:name""").literal.run().success.value should be(Literal("foo", URI("http://xmlns.com/foaf/name")))
    newParser("""-3.14""").literal.run().success.value should be(Literal("-3.14", xsd.decimal))
    newParser("""true""").literal.run().success.value should be(xsd.`true`)
  }

  "parse Var" in {
    newParser("""?name""").VAR1.run().success.value should be(Var("name"))
  }

  "parse triples" in {
    val parser = newParser("""_:betehess foaf:name "Alexandre Bertails"""")
//    parser.triples.run() match {
//      case Failure(error: ParseError) =>
//        println(parser.formatError(error))
//    }
    val parsedTriples = parser.triples.run().success.value
    parsedTriples should be(
      Vector(model.Triple(
        Concrete(parser.bnodeMap("betehess")),
        URI("http://xmlns.com/foaf/name"),
        Concrete(Literal("Alexandre Bertails"))
      ))
    )
  }

  "parse Add" in {
    val parser = newParser("""Add { _:betehess foaf:name "Alexandre Bertails" } .""")
    val parsedAdd = parser.add.run().success.value
    parsedAdd should be(
      Add(Vector(model.Triple(
        Concrete(parser.bnodeMap("betehess")),
        URI("http://xmlns.com/foaf/name"),
        Concrete(Literal("Alexandre Bertails"))
      )))
    )
  }


  "parse Complex Add" in {
    val parser = newParser("""Add {
  ?ted schema:location [
    schema:name "Long Beach, California" ;
    schema:geo [
      schema:latitude "33.7817" ;
      schema:longitude "-118.2054"
    ]
  ]
} .""")

    val parsedAdd = parser.add.run().success.value

    val equivalentGraph = reader.read("""
@prefix schema: <http://schema.org/> .
[] schema:location [
  schema:name "Long Beach, California" ;
  schema:geo [
    schema:latitude "33.7817" ;
    schema:longitude "-118.2054"
  ]
]
""", "http://example.com/timbl").get

    val graph = makeGraph(parsedAdd.triples)

    assert(equivalentGraph isIsomorphicWith graph)

  }

  "parse Add List" in {
    val parser = newParser("""Add { _:betehess foaf:name "Alexandre Bertails", "Betehess" } .""")
    val parsedAdd = parser.add.run().success.value
    parsedAdd should be(
      Add(Vector(
        model.Triple(
          Concrete(parser.bnodeMap("betehess")),
          URI("http://xmlns.com/foaf/name"),
          Concrete(Literal("Alexandre Bertails"))
        ),
        model.Triple(
          Concrete(parser.bnodeMap("betehess")),
          URI("http://xmlns.com/foaf/name"),
          Concrete(Literal("Betehess"))
        )
      ))
    )
  }

  "parse Delete" in {
    newParser("""Delete { ?betehess foaf:name "Alexandre Bertails" } .""").delete.run().success.value should be(
      Delete(Vector(model.Triple(
        Var("betehess"),
        URI("http://xmlns.com/foaf/name"),
        Concrete(Literal("Alexandre Bertails"))
      )))
    )
  }

  "parse Cut" in {
    newParser("""Cut ?betehess .""").cut.run().success.value should be(Cut(Var("betehess")))
// TODO should not compile    newParser("""Cut <http://example.com/foo> .""").cut.run().success.value should be(Cut(Concrete(URI("http://example.com/foo"))))
  }

  "parse Path" in {
    newParser("""/foaf:name/^foaf:name/<http://example.com/foo>""").path.run().success.value should be(
      Path(Seq(
        StepForward(URI("http://xmlns.com/foaf/name")),
        StepBackward(URI("http://xmlns.com/foaf/name")),
        StepForward(URI("http://example.com/foo"))
      ))
    )

    newParser("""[/<http://example.com/foo>/foaf:name="Alexandre Bertails"]""").path.run().success.value should be(
      Path(Seq(
        Filter(
          Path(Seq(
            StepForward(URI("http://example.com/foo")),
            StepForward(URI("http://xmlns.com/foaf/name"))
          )),
          Some(Concrete(Literal("Alexandre Bertails")))
        )
      ))
    )

    newParser("""[/<http://example.com/foo>/^foaf:name]/foaf:friend""").path.run().success.value should be(
      Path(Seq(
        Filter(
          Path(Seq(
            StepForward(URI("http://example.com/foo")),
            StepBackward(URI("http://xmlns.com/foaf/name"))
          )),
          None
        ),
        StepForward(URI("http://xmlns.com/foaf/friend"))
      ))
    )

    newParser("""/foaf:name!/42""").path.run().success.value should be(
      Path(Seq(
        StepForward(URI("http://xmlns.com/foaf/name")),
        UnicityConstraint,
        StepAt(42)
      ))
    )

    newParser("""/foaf:knows [ /foaf:holdsAccount /foaf:accountName = "bertails" ]""").path.run().success.value should be(
      Path(Seq(
        StepForward(URI("http://xmlns.com/foaf/knows")),
        Filter(
          Path(Seq(
            StepForward(URI("http://xmlns.com/foaf/holdsAccount")),
            StepForward(URI("http://xmlns.com/foaf/accountName"))
          )),
          Some(Concrete(Literal("bertails")))
        )
      ))
    )

  }


  "parse Bind" in {

    newParser("""Bind ?foo <http://example.com/blah> .""").bind.run().success.value should be(
      Bind(
        Var("foo"),
        Concrete(URI("http://example.com/blah")),
        Path(Seq())
      )
    )

    newParser("""Bind ?foo <http://example.com/blah> /foaf:name/^foaf:name/<http://example.com/foo> .""").bind.run().success.value should be(
      Bind(
        Var("foo"),
        Concrete(URI("http://example.com/blah")),
        Path(Seq(
          StepForward(URI("http://xmlns.com/foaf/name")),
          StepBackward(URI("http://xmlns.com/foaf/name")),
          StepForward(URI("http://example.com/foo"))
        ))
      )
    )

    val bind = """Bind ?alex
     <http://champin.net/#pa>/foaf:knows
        [/foaf:holdsAccount/foaf:accountName="bertails"] ."""
    newParser(bind).bind.run().success.value should be(
      Bind(
        Var("alex"),
        Concrete(URI("http://champin.net/#pa")),
        Path(Seq(
          StepForward(URI("http://xmlns.com/foaf/knows")),
          Filter(
            Path(Seq(
              StepForward(URI("http://xmlns.com/foaf/holdsAccount")),
              StepForward(URI("http://xmlns.com/foaf/accountName"))
            )),
            Some(Concrete(Literal("bertails")))
          )
        ))
      )
    )


  }

  "parse Slice" in {
    newParser("""42..2868""").slice.run().success.value should be(Range(42, 2868))
    newParser("""42..""").slice.run().success.value should be(EverythingAfter(42))
    newParser("""..2868""").slice.run().success.value should be(Range(0, 2868))
    newParser("""..""").slice.run().success.value should be(End)
  }

  "parse UpdateList" in {
    newParser("""UpdateList ?alex foaf:prefLang 0.. ( "fr" "en" ) .""").updateList.run() should be('Success)
  }

  "parse Prologue" in {
    newFreshParser("""@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix v: <http://example.org/vocab#> .""").prologue.run().success.value should be(
      Map("rdf" -> URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"), "foaf" -> URI("http://xmlns.com/foaf/0.1/"), "v" -> URI("http://example.org/vocab#"))
    )
  }

  "parse LDPatch" in {
    val patch = """
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix v: <http://example.org/vocab#> .

Bind ?alex
     <http://champin.net/#pa> /foaf:knows
        [/foaf:holdsAccount/foaf:accountName="bertails"] .

UpdateList ?alex v:prefLang 0.. ( "fr" "en" ) .
"""
    newFreshParser(patch).ldpatch.run() should be('Success)
    
  }

  "parse Example 2 from spec" in {
    val patch = """
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix schema: <http://schema.org> .
@prefix profile: <http://ogp.me/ns/profile#> .
@prefix ex: <http://example.org/vocab#> .

Delete { <#> profile:first_name "Tim" } .
Add    { <#> profile:first_name "Timothy" } .

UpdateList <#> ex:preferredLanguages 1..2 ( "fr-CH" ) .

Bind ?event <#> /schema:attendee[/schema:url = <http://conferences.ted.com/TED2009/>]  .
Add { ?event rdf:type schema:Event } .

Bind ?ted <http://conferences.ted.com/TED2009/> /^schema:url! .
Delete { ?ted schema:startDate "2009-02-04" } .
Add { ?ted schema:location _:loc } .
Add { _:loc schema:name "Long Beach, California" } .
Add { _:loc schema:geo _:geo } .
Add { _:geo schema:latitude "33.7817" } .
Add { _:geo schema:longitude "-118.2054" } .
"""
//    val parser = newFreshParser(patch)
//    parser.ldpatch.run() match {
//      case Failure(error: ParseError) =>
//        println(parser.formatError(error))
//    }

    newFreshParser(patch).ldpatch.run() should be a ('Success)
  }

  "parse Example 2 from spec - v2" in {
    val patch = """
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix schema: <http://schema.org> .
@prefix profile: <http://ogp.me/ns/profile#> .
@prefix ex: <http://example.org/vocab#> .

Delete { <#> profile:first_name "Tim" } .
Add    { <#> profile:first_name "Timothy" } .

UpdateList <#> ex:preferredLanguages 1..2 ( "fr-CH" ) .

Bind ?event <#> /schema:attendee[/schema:url = <http://conferences.ted.com/TED2009/>]  .
Add { ?event rdf:type schema:Event } .

Bind ?ted <http://conferences.ted.com/TED2009/> /^schema:url! .
Delete { ?ted schema:startDate "2009-02-04" } .
Add {
  ?ted schema:location [
    schema:name "Long Beach, California" ;
    schema:geo [
      schema:latitude "33.7817" ;
      schema:longitude "-118.2054"
    ]
  ]
} .
"""
    newFreshParser(patch).ldpatch.run() should be a ('Success)
  }


}

import org.w3.banana.jena._

class JenaLDPatchGrammarTest extends LDPatchGrammarTest[Jena]

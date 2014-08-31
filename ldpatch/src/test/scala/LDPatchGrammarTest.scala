package org.w3.banana.ldpatch

import org.w3.banana.{ Delete => _, _ }
import org.scalatest.{ Filter => _, _ }
import java.io._
import scala.util.{ Try, Success, Failure }
import org.w3.banana.ldpatch.model._

abstract class LDPatchGrammarTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers with TryValues { self =>

  import ops._

  val g = new Grammar[Rdf] { implicit val ops = self.ops }

  def newParser(input: String) =
    new g.grammar.PEGPatchParser(input, baseURI = URI("http://example.com/foo"), prefixes = Map("foaf" -> URI("http://xmlns.com/foaf/")))

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
    newParser("""?name""").Var.run().success.value should be(Var("name"))
  }

  "parse Add Object" in {
    val parser = newParser("""Add _:betehess foaf:name "Alexandre Bertails" .""")
    val parsedAdd = parser.Add.run().success.value
    parsedAdd should be(
      Add(
        Concrete(parser.bnodeMap("betehess")),
        URI("http://xmlns.com/foaf/name"),
        Concrete(Literal("Alexandre Bertails"))
      )
    )
  }

  "parse Add List" in {
    val parser = newParser("""Add _:betehess foaf:name ( "Alexandre Bertails" "Betehess" ).""")
    val parsedAdd = parser.Add.run().success.value
    parsedAdd should be(
      AddList(
        Concrete(parser.bnodeMap("betehess")),
        URI("http://xmlns.com/foaf/name"),
        Seq(Concrete(Literal("Alexandre Bertails")), Concrete(Literal("Betehess")))
      )
    )
  }

  "parse Delete" in {
    newParser("""Delete ?betehess foaf:name "Alexandre Bertails" .""").Delete.run().success.value should be(
      Delete(
        Var("betehess"),
        URI("http://xmlns.com/foaf/name"),
        Concrete(Literal("Alexandre Bertails"))
      )
    )
  }

  "parse Path" in {
    newParser("""/foaf:name/-foaf:name/<http://example.com/foo>""").Path.run().success.value should be(
      Path(Seq(
        StepForward(URI("http://xmlns.com/foaf/name")),
        StepBackward(URI("http://xmlns.com/foaf/name")),
        StepForward(URI("http://example.com/foo"))
      ))
    )

    newParser("""[/<http://example.com/foo>/foaf:name="Alexandre Bertails"]""").Path.run().success.value should be(
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

    newParser("""[/<http://example.com/foo>/-foaf:name]/foaf:friend""").Path.run().success.value should be(
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

    newParser("""/foaf:name!/42""").Path.run().success.value should be(
      Path(Seq(
        StepForward(URI("http://xmlns.com/foaf/name")),
        UnicityConstraint,
        StepAt(42)
      ))
    )

    newParser("""/foaf:knows [ /foaf:holdsAccount /foaf:accountName = "bertails" ]""").Path.run().success.value should be(
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

    newParser("""Bind ?foo <http://example.com/blah> .""").Bind.run().success.value should be(
      Bind(
        Var("foo"),
        Concrete(URI("http://example.com/blah")),
        Path(Seq())
      )
    )

    newParser("""Bind ?foo <http://example.com/blah> /foaf:name/-foaf:name/<http://example.com/foo> .""").Bind.run().success.value should be(
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
    newParser(bind).Bind.run().success.value should be(
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
    newParser("""42>2868""").Slice.run().success.value should be(Range(42, 2868))
    newParser("""42>""").Slice.run().success.value should be(EverythingAfter(42))
    newParser(""">2868""").Slice.run().success.value should be(Range(0, 2868))
    newParser(""">""").Slice.run().success.value should be(End)
  }

  "parse UpdateList" in {
    newParser("""UpdateList ?alex foaf:prefLang 0> ( "fr" "en" ) .""").UpdateList.run().success.value should be(
      UpdateList(Var("alex"), URI("http://xmlns.com/foaf/prefLang"), EverythingAfter(0), Seq(Concrete(Literal("fr")), Concrete(Literal("en"))))
    )
  }

  "parse Prologue" in {
    newFreshParser("""@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix v: <http://example.org/vocab#> .""").Prologue.run().success.value should be(
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

UpdateList ?alex v:prefLang 0> ( "fr" "en" ) .
"""
    newFreshParser(patch).LDPatch.run().success.value should be(
      model.LDPatch(Seq(
        Bind(
          Var("alex"),
          Concrete(URI("http://champin.net/#pa")),
          Path(Seq(
            StepForward(URI("http://xmlns.com/foaf/0.1/knows")),
            Filter(
              Path(Seq(
                StepForward(URI("http://xmlns.com/foaf/0.1/holdsAccount")),
                StepForward(URI("http://xmlns.com/foaf/0.1/accountName"))
              )),
              Some(Concrete(Literal("bertails")))
            )
          ))
        ),
        UpdateList(
          Var("alex"),
          URI("http://example.org/vocab#prefLang"),
          EverythingAfter(0),
          Seq(Concrete(Literal("fr")), Concrete(Literal("en")))
        )
      ))
    )
    
  }

  "parse Example 2 from spec" in {
    val patch = """
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix schema: <http://schema.org> .
@prefix profile: <http://ogp.me/ns/profile#> .
@prefix ex: <http://example.org/vocab#> .

Delete <#> profile:first_name "Tim" .
Add    <#> profile:first_name "Timothy" .

UpdateList <#> ex:preferredLanguages 1>2 ( "fr-CH" ) .

Bind ?event <#> /schema:attendee[/schema:url = <http://conferences.ted.com/TED2009/>]  .
Add ?event rdf:type schema:Event .

Bind ?ted <http://conferences.ted.com/TED2009/> /-schema:url! .
Delete ?ted schema:startDate "2009-02-04".
Add ?ted schema:location _:loc .
Add _:loc schema:name "Long Beach, California" .
Add _:loc schema:geo _:geo .
Add _:geo schema:latitude "33.7817" .
Add _:geo schema:longitude "-118.2054" .
"""
    newFreshParser(patch).LDPatch.run() should be a ('Success)
  }


}

import org.w3.banana.jena._

class JenaLDPatchGrammarTest extends LDPatchGrammarTest[Jena]

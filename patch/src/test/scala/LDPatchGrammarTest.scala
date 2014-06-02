package org.w3.banana.ldpatch

import org.w3.banana.{ Delete => _, _ }
import org.scalatest.{ Filter => _, _ }
import java.io._
import scala.util.{ Try, Success, Failure }
import org.w3.banana.ldpatch.model._

abstract class LDPatchGrammarTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers with TryValues {

  import ops._

  val ldpatch = LDPatch[Rdf]

  def newParser(input: String) =
    new ldpatch.grammar.PEGPatchParser(input, baseURI = URI("http://example.com/foo#"), prefixes = Map("foaf" -> URI("http://xmlns.com/foaf/")))

  def newFreshParser(input: String) =
    new ldpatch.grammar.PEGPatchParser(input, baseURI = URI("http://example.com/foo#"), prefixes = Map.empty)

  "parse IRIREF" in {
    newParser("""<http://example.com/foo#\u2665>""").IRIREF.run().success.value should be(URI("http://example.com/foo#♥"))
  }

  "parse iri" in {
    newParser("""<http://example.com/foo#\u2665>""").iri.run().success.value should be(URI("http://example.com/foo#♥"))
    newParser("""foaf:name""").iri.run().success.value should be(URI("http://xmlns.com/foaf/name"))
  }

  "parse Prefix" in {
    newParser("""Prefix example:<http://example.com/foo#>""").Prefix.run().success.value should be("example" -> URI("http://example.com/foo#"))
    newParser("""Prefix   example: <http://example.com/foo#>""").Prefix.run().success.value should be("example" -> URI("http://example.com/foo#"))
    newParser("""Prefix : <http://example.com/foo#>""").Prefix.run().success.value should be("" -> URI("http://example.com/foo#"))
  }

  "parse BlankNode" in {
    newParser("""_:foo""").BlankNode.run().success.value should be(BNode("foo"))
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
    newParser("""Add _:betehess foaf:name "Alexandre Bertails"""").Add.run().success.value should be(
      Add(
        PatchBNode(BNode("betehess")),
        PatchIRI(URI("http://xmlns.com/foaf/name")),
        PatchLiteral(Literal("Alexandre Bertails"))
      )
    )
  }

  "parse Add List" in {
    newParser("""Add _:betehess foaf:name ( "Alexandre Bertails" "Betehess" )""").Add.run().success.value should be(
      AddList(
        PatchBNode(BNode("betehess")),
        PatchIRI(URI("http://xmlns.com/foaf/name")),
        Seq(PatchLiteral(Literal("Alexandre Bertails")), PatchLiteral(Literal("Betehess")))
      )
    )
  }

  "parse Delete" in {
    newParser("""Delete ?betehess foaf:name "Alexandre Bertails"""").Delete.run().success.value should be(
      Delete(
        Var("betehess"),
        PatchIRI(URI("http://xmlns.com/foaf/name")),
        PatchLiteral(Literal("Alexandre Bertails"))
      )
    )
  }

  "parse Path" in {
    newParser("""/foaf:name/-foaf:name/<http://example.com/foo>""").Path.run().success.value should be(
      Path(Seq(
        StepForward(PatchIRI(URI("http://xmlns.com/foaf/name"))),
        StepBackward(PatchIRI(URI("http://xmlns.com/foaf/name"))),
        StepForward(PatchIRI(URI("http://example.com/foo")))
      ))
    )

    newParser("""[/<http://example.com/foo>/foaf:name="Alexandre Bertails"]""").Path.run().success.value should be(
      Path(Seq(
        Filter(
          Path(Seq(
            StepForward(PatchIRI(URI("http://example.com/foo"))),
            StepForward(PatchIRI(URI("http://xmlns.com/foaf/name")))
          )),
          Some(PatchLiteral(Literal("Alexandre Bertails")))
        )
      ))
    )

    newParser("""[/<http://example.com/foo>/-foaf:name]/foaf:friend""").Path.run().success.value should be(
      Path(Seq(
        Filter(
          Path(Seq(
            StepForward(PatchIRI(URI("http://example.com/foo"))),
            StepBackward(PatchIRI(URI("http://xmlns.com/foaf/name")))
          )),
          None
        ),
        StepForward(PatchIRI(URI("http://xmlns.com/foaf/friend")))
      ))
    )

    newParser("""/foaf:name!/42""").Path.run().success.value should be(
      Path(Seq(
        StepForward(PatchIRI(URI("http://xmlns.com/foaf/name"))),
        UnicityConstraint,
        StepAt(42)
      ))
    )

    newParser("""/foaf:knows [ /foaf:holdsAccount /foaf:accountName = "bertails" ]""").Path.run().success.value should be(
      Path(Seq(
        StepForward(PatchIRI(URI("http://xmlns.com/foaf/knows"))),
        Filter(
          Path(Seq(
            StepForward(PatchIRI(URI("http://xmlns.com/foaf/holdsAccount"))),
            StepForward(PatchIRI(URI("http://xmlns.com/foaf/accountName")))
          )),
          Some(PatchLiteral(Literal("bertails")))
        )
      ))
    )

  }


  "parse Bind" in {

    newParser("""Bind ?foo <http://example.com/blah>""").Bind.run().success.value should be(
      Bind(
        Var("foo"),
        PatchIRI(URI("http://example.com/blah")),
        Path(Seq())
      )
    )

    newParser("""Bind ?foo <http://example.com/blah> /foaf:name/-foaf:name/<http://example.com/foo>""").Bind.run().success.value should be(
      Bind(
        Var("foo"),
        PatchIRI(URI("http://example.com/blah")),
        Path(Seq(
          StepForward(PatchIRI(URI("http://xmlns.com/foaf/name"))),
          StepBackward(PatchIRI(URI("http://xmlns.com/foaf/name"))),
          StepForward(PatchIRI(URI("http://example.com/foo")))
        ))
      )
    )

    val bind = """Bind ?alex
     <http://champin.net/#pa>/foaf:knows
        [/foaf:holdsAccount/foaf:accountName="bertails"]"""
    newParser(bind).Bind.run().success.value should be(
      Bind(
        Var("alex"),
        PatchIRI(URI("http://champin.net/#pa")),
        Path(Seq(
          StepForward(PatchIRI(URI("http://xmlns.com/foaf/knows"))),
          Filter(
            Path(Seq(
              StepForward(PatchIRI(URI("http://xmlns.com/foaf/holdsAccount"))),
              StepForward(PatchIRI(URI("http://xmlns.com/foaf/accountName")))
            )),
            Some(PatchLiteral(Literal("bertails")))
          )
        ))
      )
    )


  }

  "parse Slice" in {
    newParser("""42>2868""").Slice.run().success.value should be(Range(42, 2868))
    newParser("""42>""").Slice.run().success.value should be(EverythingAfter(42))
    newParser(""">2868""").Slice.run().success.value should be(EverythingBefore(2868))
    newParser(""">""").Slice.run().success.value should be(End)
  }

  "parse Replace" in {
    newParser("""Replace ?alex foaf:prefLang 0> ( "fr" "en" )""").Replace.run().success.value should be(
      Replace(Var("alex"), PatchIRI(URI("http://xmlns.com/foaf/prefLang")), EverythingAfter(0), Seq(PatchLiteral(Literal("fr")), PatchLiteral(Literal("en"))))
    )
  }

  "parse Prologue" in {
    newFreshParser("""Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
Prefix foaf: <http://xmlns.com/foaf/0.1/>
Prefix v: <http://example.org/vocab#>""").Prologue.run().success.value should be(
      Map("rdf" -> URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"), "foaf" -> URI("http://xmlns.com/foaf/0.1/"), "v" -> URI("http://example.org/vocab#"))
    )
  }

  "parse LDPatch" in {
    val patch = """
Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
Prefix foaf: <http://xmlns.com/foaf/0.1/>
Prefix v: <http://example.org/vocab#>

Bind ?alex
     <http://champin.net/#pa> /foaf:knows
        [/foaf:holdsAccount/foaf:accountName="bertails"]

Replace ?alex v:prefLang 0> ( "fr" "en" )
"""
    newFreshParser(patch).LDPatch.run().success.value should be(
      model.LDPatch(Seq(
        Bind(
          Var("alex"),
          PatchIRI(URI("http://champin.net/#pa")),
          Path(Seq(
            StepForward(PatchIRI(URI("http://xmlns.com/foaf/0.1/knows"))),
            Filter(
              Path(Seq(
                StepForward(PatchIRI(URI("http://xmlns.com/foaf/0.1/holdsAccount"))),
                StepForward(PatchIRI(URI("http://xmlns.com/foaf/0.1/accountName")))
              )),
              Some(PatchLiteral(Literal("bertails")))
            )
          ))
        ),
        Replace(
          Var("alex"),
          PatchIRI(URI("http://example.org/vocab#prefLang")),
          EverythingAfter(0),
          Seq(PatchLiteral(Literal("fr")), PatchLiteral(Literal("en")))
        )
      ))
    )

    
  }


}

import org.w3.banana.jena._

class JenaLDPatchGrammarTest extends LDPatchGrammarTest[Jena]

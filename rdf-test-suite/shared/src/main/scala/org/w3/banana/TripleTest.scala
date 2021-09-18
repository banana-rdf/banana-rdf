package org.w3.banana

import org.w3.banana.TestConstants.{bbl, foafPre, tim}
import org.w3.banana.prefix.{FOAF, XSD}
import org.w3.banana.{Ops, RDF}

import scala.reflect.TypeTest


open class TripleTest[R<:RDF](using ops: Ops[R])
	extends munit.FunSuite:
	//a lot of imports
	import RDF.*
	import ops.{given,*}
	import org.w3.banana.syntax.*
	import org.w3.banana.syntax.LiteralW.*
	import org.w3.banana.syntax.LangW.*

	val timbl: URI[R] = URI(tim("i"))
	val bblf: URI[R] = URI(bbl("i"))
	val xsd:  XSD[R] = XSD[R]
	val foaf: FOAF[R] = FOAF[R]

	test("type test on literal") {
		// this gives a warning: "cannot call the type test at runtime"!
		import ops.given TypeTest[Matchable, RDF.Literal[R]]
		val timNode: RDF.Node[R] = Literal("Tim")
		timNode match
		case t : RDF.Literal[R] =>
			assert(true,"good")
		case _ => fail("could not match literal")
	}

	test("Literal Tests") {
		val timLit: RDF.Literal[R] = Literal("Tim")
		timLit match
			case t : RDF.Literal[R] =>
				assert(true, "can't fail to get here")
				//assertEquals(t.text, "Tim")

		val hname: RDF.Literal[R] = "Henry" `@` Lang("en")

		hname match
			case Literal(n `@` l) =>
				assertEquals(n, "Henry")
				assertEquals(l, Lang("en"))

		val hnode: RDF.Node[R] = hname
		hnode match
			case Literal(n `@` l) =>
				assertEquals(n, "Henry")
				assertEquals(l, Lang("en"))
			case _ => fail(s"ca not match $hnode as a lang node ")
	}

	test("triple tests") {
		val bkt = Triple(bblf,foaf.knows,timbl)
		bkt match
			case Triple(t) =>
				t match
					case (b,k,t) =>
						assertEquals[RDF.Node[R],RDF.Node[R]](t,timbl)
						assertEquals(k,foaf.knows)
						assertEquals[RDF.Node[R],RDF.Node[R]](b,bblf)
		val tkb = Triple(timbl,foaf.knows,bblf)
		tkb match
			case Triple(t,k,b) =>
				assertEquals[RDF.Node[R],RDF.Node[R]](t,timbl)
				assertEquals(k,foaf.knows)
				assertEquals[RDF.Node[R],RDF.Node[R]](b,bblf)
			case _ => fail("failed to match the triple we constructed")
		val tname = Triple(timbl, foaf.name, "Tim"`@`Lang("en"))
		tname match
			case Triple(t, p, Literal(name `@` lang)) =>
				assertEquals[RDF.Node[R],RDF.Node[R]](t,timbl)
				assertEquals(p,foaf.name)
				assertEquals(name,"Tim")
				assertEquals(lang, Lang("en"))
			case _ => fail(s"could not match $tname")
		val byear = "1967"^^xsd.integer
		val bbyear = Triple(bblf,URI(foafPre("byear")),byear) //note: byear does not exist in foaf
		// this does not work with TypeTests. Try again when we can express Literal[R] <: Node[R]
		bbyear match
			case Triple(s, p, l: RDF.Literal[R]) =>
				assertEquals[RDF.Node[R],RDF.Node[R]](s, bblf)
		bbyear match
			case Triple(s, p, Literal(l)) =>
				assertEquals[RDF.Node[R],RDF.Node[R]](s, bblf)
				assertEquals(l.text,"1967")
			case _ => fail("pattern did not match")

		bbyear match
			case Triple(s, p, Literal(yearStr ^^ xsd.integer)) =>
				assertEquals[RDF.Node[R],RDF.Node[R]](s, bblf)
				assertEquals(yearStr,"1967")
			case _ => fail("pattern did not match")
	}

end TripleTest

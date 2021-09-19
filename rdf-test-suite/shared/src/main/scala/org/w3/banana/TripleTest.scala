package org.w3.banana

import org.w3.banana.TestConstants.{bbl, foafPre, tim}
import org.w3.banana.prefix.{FOAF, XSD}
import org.w3.banana.{Ops, RDF}

import scala.reflect.TypeTest

/**
 * we cover here URI, BNode, Literal and Triple tests
 */
open class TripleTest[R<:RDF](using ops: Ops[R])
	extends munit.FunSuite:
	//a lot of imports
	import RDF.*
	import ops.{given,*}
	import org.w3.banana.syntax.*

	test("BNode test") {
		val bn = BNode()
		val bn1 = BNode("b1")
		assertEquals(bn1.label,"b1")
		assert(bn.label != bn1.label)
	}
	
	val xsd:  XSD[R] = XSD[R]
	val foaf: FOAF[R] = FOAF[R]
	val timbl: URI[R] = URI(tim("i"))
	val bblf: URI[R] = URI(bbl("i"))

	test("URI Test") {
		assertEquals(timbl.toString,tim("i"))
		assertEquals(bblf.toString, bbl("i"))
	}

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
		assertEquals(timLit.text,"Tim")
		timLit match
			case t : RDF.Literal[R] =>
				assert(true, "can't fail to get here")
				//assertEquals(t.text, "Tim")

		val hname: RDF.Literal[R] = "Henry" `@` Lang("en")
		assertEquals(hname.text,"Henry")

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

		val age = "999" ^^ xsd.integer
		assertEquals(age.text,"999")
		age match
			case Literal(y `^^` t) =>
				assertEquals(y, "999")
				assertEquals(t, xsd.integer)
			case _ => fail(s"can not match $age as a datatype ")
		val dtEx = List(timLit,hname,age," 2001-10-26T21:32:52+02:00"^^xsd.dateTime)
		val trans = dtEx.map{
			case Literal(li) =>
				li match
				case Plain(text) => text
				case t `@` l => t+":"+l
				case t ^^ xsd.integer => "#"+t
				case _ => "other"
		}
		assertEquals(trans,List("Tim","Henry:en","#999","other"))
		val trans2 = dtEx.map((lit: Literal[R]) => lit.fold[String](
			identity,
			(t,l) =>  t+":"+l,
			(t,dt) => if dt == xsd.integer then "#"+t else "other"
		))
		assertEquals(trans2, trans)
	}

	test("Node Tests") {

	}

	test("triple tests") {
		val bkt = Triple(bblf,foaf.knows,timbl)
		assertEquals[Node[R],Node[R]](bkt.subj,bblf)
		assertEquals[Node[R],Node[R]](bkt.rel,foaf.knows)
		assertEquals[Node[R],Node[R]](bkt.obj,timbl)

		bkt match
			case Triple(t) =>
				t match
					case (b,k,t) =>
						assertEquals[Node[R],Node[R]](t,timbl)
						assertEquals(k,foaf.knows)
						assertEquals[Node[R],Node[R]](b,bblf)
		val tkb = Triple(timbl,foaf.knows,bblf)
		tkb match
			case Triple(t,k,b) =>
				assertEquals[Node[R],Node[R]](t,timbl)
				assertEquals(k,foaf.knows)
				assertEquals[Node[R],Node[R]](b,bblf)
			case _ => fail("failed to match the triple we constructed")
		val tname = Triple(timbl, foaf.name, "Tim"`@`Lang("en"))
		tname match
			case Triple(t, p, Literal(name `@` lang)) =>
				assertEquals[Node[R],Node[R]](t,timbl)
				assertEquals(p,foaf.name)
				assertEquals(name,"Tim")
				assertEquals(lang, Lang("en"))
			case _ => fail(s"could not match $tname")
		val byear = "1967"^^xsd.integer
		val bbyear = Triple(bblf,URI(foafPre("byear")),byear) //note: byear does not exist in foaf
		// this does not work with TypeTests. Try again when we can express Literal[R] <: Node[R]
		bbyear match
			case Triple(s, p, l: Literal[R]) =>
				assertEquals[Node[R],Node[R]](s, bblf)
		bbyear match
			case Triple(s, p, Literal(l)) =>
				assertEquals[Node[R],Node[R]](s, bblf)
				assertEquals(l.text,"1967")
			case _ => fail("pattern did not match")

		bbyear match
			case Triple(s, p, Literal(yearStr ^^ xsd.integer)) =>
				assertEquals[Node[R],Node[R]](s, bblf)
				assertEquals(yearStr,"1967")
			case _ => fail("pattern did not match")
	}

end TripleTest

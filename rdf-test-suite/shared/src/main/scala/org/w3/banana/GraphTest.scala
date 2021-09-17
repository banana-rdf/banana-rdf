package org.w3.banana

import org.w3.banana.RDF.*
import TestConstants.*

open class GraphTest[Rdf<:RDF](using ops: Ops[Rdf]) extends munit.FunSuite {
	//todo: find a way to simplify these imports for end users of the library
	import ops.{given,*}
	import org.w3.banana.syntax.*
	import org.w3.banana.syntax.LiteralW.*
	import org.w3.banana.syntax.LangW.*

	val timbl: URI[Rdf] = URI(tim("i"))
	val fknows: URI[Rdf] = URI(foaf("knows"))
	val fname: URI[Rdf] = URI(foaf("name"))
	val bblf: URI[Rdf] = URI(bbl("i"))

	test("Test Graph syntax methods") {
		val g0: Graph[Rdf] = Graph.empty
		assertEquals(g0.size,0)
		val bkt = Triple(bblf,fknows,timbl)
		bkt match
			case Triple(t) =>
				t match
				case (b,k,t) =>
					assertEquals[RDF.Node[Rdf],RDF.Node[Rdf]](t,timbl)
					assertEquals(k,fknows)
					assertEquals[RDF.Node[Rdf],RDF.Node[Rdf]](b,bblf)
		val g1 = g0 + bkt
		assertEquals(g1.size,1)
		val g1x = g1.union(g1)
		assertEquals(g1x.size,1)
		assert(g1.isIsomorphicWith(g1x))
		val tkb = Triple(timbl,fknows,bblf)
		tkb match
			case Triple(t,k,b) =>
				assertEquals[RDF.Node[Rdf],RDF.Node[Rdf]](t,timbl)
				assertEquals(k,fknows)
				assertEquals[RDF.Node[Rdf],RDF.Node[Rdf]](b,bblf)
			case _ => fail("failed to match the triple we constructed")
		val g2 = g1 + tkb
		assertEquals(g2.size,2)
		val g2LessG1 = g2.diff(g1)
		assertEquals(g2LessG1.size,1)
		assertEquals(g2LessG1.triples.head,tkb)
		val tname = Triple(timbl, fname, "Tim"`@`Lang("en"))
		tname match
			case Triple(t,p,Literal(LiteralI.`@`(name, lang))) =>
				assertEquals[RDF.Node[Rdf],RDF.Node[Rdf]](t,timbl)
				assertEquals(p,fname)
				assertEquals(name,"Tim")
				assertEquals(lang, Lang("en"))
			case _ => fail(s"could not match $tname")
	}

	test("relative graphs") {
		val rg0 = rGraph()
		assertEquals(rg0.rsize,0)
		val rg1 = rGraph(rTriple(rURI("/#i"),URI(foaf("homePage")),rURI("/")))
		assertEquals(rg1.rsize,1)

		//tests to check that opaque types work: we cannot just cast rX down to X
		val err1 = compileErrors(
			"""val normalUri : URI[Rdf] = rURI("/#i")"""
		)
		assert(err1.contains("Required: org.w3.banana.RDF.URI[Rdf]"))

		val err2 = compileErrors(
			"""val normalTriple : Triple[Rdf] =
			    rTriple(rURI("/#i"),URI(foaf("homePage")),rURI("/"))"""
		)
		assert(err2.contains("Required: org.w3.banana.RDF.Triple[Rdf]"))

		val err3 = compileErrors(
			"""val normalGraph : Graph[Rdf] =
			    rGraph(rTriple(rURI("/#i"),URI(foaf("homePage")),rURI("/")))"""
		)
		assert(err3.contains("Required: org.w3.banana.RDF.Graph[Rdf]"))


	}

}

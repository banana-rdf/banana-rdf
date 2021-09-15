package org.w3.banana

import org.w3.banana.RDF.*
import org.w3.banana.TestConstants.tim

open class GraphTest[Rdf<:RDFObj](using ops: Ops[Rdf]) extends munit.FunSuite {
	import ops.*
	import TestConstants.*
	import org.w3.banana.syntax.*

	val timbl: URI[Rdf] = URI(tim("i"))
	val knows: URI[Rdf] = URI(foaf("knows"))
	val bblf: URI[Rdf] = URI(bbl("i"))

	test("Test Graph syntax methods") {
		val g0: Graph[Rdf] = Graph.empty
		assertEquals(g0.size,0)
		val g1 = g0.union(Graph(Triple(bblf,knows,timbl)))
		assertEquals(g1.size,1)
		val g1x = g1.union(g1)
		assertEquals(g1x.size,1)
		assert(g1.isIsomorphicWith(g1x))
		val tkb = Triple(timbl,knows,bblf)
		val g2 = g1 + tkb
		assertEquals(g2.size,2)
		val g2LessG1 = g2.diff(g1)
		assertEquals(g2LessG1.size,1)
		assertEquals(g2LessG1.triples.head,tkb)
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

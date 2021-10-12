package org.w3.banana

import org.w3.banana.RDF.*
import TestConstants.*
import org.w3.banana.prefix.XSD

open class GraphTest[Rdf<:RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
	//todo: find a way to simplify these imports for end users of the library
	import ops.{given,*}
	import org.w3.banana.prefix.{FOAF,XSD}
	import org.w3.banana.syntax.*

	val timbl: URI[Rdf] = URI(tim("i"))
	val bblf: URI[Rdf] = URI(bbl("i"))
	val xsd: XSD[Rdf] = XSD[Rdf]
	val foaf: FOAF[Rdf] = FOAF[Rdf]

	test("empty graph contains no triples") {
		val g0: Graph[Rdf] = Graph.empty
		assertEquals(g0.size,0)
	}

	test("Add a triple to a graph and it becomes one bigger") {
		val bkt = Triple(bblf,foaf.knows,timbl)
		val g1 = Graph.empty + bkt
		assertEquals(g1.size,1)
	}

	test("Test Graph syntax methods") {
		val g0: Graph[Rdf] = Graph.empty
		val bkt = Triple(bblf,foaf.knows,timbl)
		val g1 = g0 + bkt
		assertEquals(g1.size,1)
		val g1x = g1.union(g1)
		assertEquals(g1x.size,1)
		assert(g1 isomorphic g1x)
		val tkb = Triple(timbl,foaf.knows,bblf)
		val g2 = g1 + tkb
		assertEquals(g2.size,2)
		val g2LessG1 = g2.diff(g1)
		assertEquals(g2LessG1.size,1)
		assertEquals(g2LessG1.triples.head,tkb)
		val tname = Triple(timbl, foaf.name, "Tim"`@`Lang("en"))
		val bbyear = Triple(bblf,URI(foafPre("byear")),"1967"^^xsd.integer) //note: byear does not exist in foaf
// this does not work with TypeTests. Try again when we can express Literal[R] <: Node[R]
		val glit: Graph[Rdf] = Graph(tname, bbyear)
		assertEquals(glit.size,2)
		val gbig = glit.union(g2)
		assertEquals(gbig.size,4)
		assert(gbig ≅ g2.union(glit))
	}

	test("relative graphs") {
		val rg0 = rGraph.empty
		assertEquals(rg0.rsize,0)
		val rg1 = rGraph(rTriple(rURI("/#i"),URI(foafPre("homePage")),rURI("/")))
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
end GraphTest

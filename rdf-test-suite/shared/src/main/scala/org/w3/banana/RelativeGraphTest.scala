package org.w3.banana

import org.w3.banana.TestConstants.foafPre

import org.w3.banana.RDF.*
import TestConstants.*

/** Not all frameworks support relative graphs -- or at least they require
 * different implementations */
class RelativeGraphTest[Rdf<:RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
	import ops.{given,*}
	import org.w3.banana.syntax.*


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

package org.w3.banana

import org.w3.banana.RDF.{RDFObj, URI}
import org.w3.banana.TestConstants.tim

class GraphTest[Rdf<:RDFObj](using ops: Ops[Rdf]) extends munit.FunSuite {
	import ops.*
	import TestConstants.*
	import org.w3.banana.syntax.*

	val timbl: URI[Rdf] = URI(tim("i"))
	val knows: URI[Rdf] = URI(foaf("knows"))
	val bblf: URI[Rdf] = URI(bbl("i"))

	test("") {
		val g1 = Graph(Triple(bblf,knows,timbl))
		assertEquals(g1.size,1)
		val g1x = g1.union(g1)
		assertEquals(g1x.size,1)
		val g2 = g1.union(Graph(Triple(timbl,knows,bblf)))
		assertEquals(g2.size,2)
	}

}

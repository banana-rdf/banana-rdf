package  org.w3.banana

import org.w3.banana.RDF
import RDF.*

open class PGTest[Rdf<:RDFObj](using ops: Ops[Rdf]) extends munit.FunSuite {
	import ops.*
	import TestConstants.*

	test("PG Creation") {
		assertEquals(Graph.graphSize(Graph.empty),0)
		val timbl: URI[Rdf] = URI(tim("i"))
		val timEmpty: PG[Rdf] = PG(timbl)
//		assertEquals(timbl,timEmpty.pointer)
//		assertEquals(timEmpty.graph,Graph.empty)
	}
}

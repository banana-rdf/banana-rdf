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
		//note: in order for the implicit conversion to take hold we need to specify the upper bound
		assertEquals[RDF.Node[Rdf],RDF.Node[Rdf]](timEmpty.pointer,timbl)
		assertEquals(timEmpty.graph,Graph.empty)
	}
}

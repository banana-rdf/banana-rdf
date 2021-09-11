package  org.w3.banana

import org.w3.banana.RDF
import RDF.*

open class PGTest[Rdf<:RDF & Singleton](using ops: RDFOps[Rdf]) extends munit.FunSuite {
	import ops.*

	test("PG Creation") {
		assertEquals(graphSize(empty),0)
	}
}

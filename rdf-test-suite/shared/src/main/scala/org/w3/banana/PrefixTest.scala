package org.w3.banana

import org.w3.banana.prefix.{FOAF, RDFPrefix, XSD}

open class PrefixTest[Rdf<:RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
	import ops.{given,*}
	import TestConstants.*
	val foaf = FOAF[Rdf]
	val xsd = XSD[Rdf]
	val rdf = RDFPrefix[Rdf]

	test("FOAF Prefix") {
		assertEquals(foaf.age,     URI(foafPre("age")))
		assertEquals(foaf.knows,   URI(foafPre("knows")))
		assertEquals(foaf.homepage,URI(foafPre("homepage")))
	}

	test("XSD Prefix") {
		assertEquals(xsd.int,       URI(xsdPre("int")))
		assertEquals(xsd.integer,   URI(xsdPre("integer")))
		assertEquals(xsd.hexBinary, URI(xsdPre("hexBinary")))
		assertEquals(xsd.string, URI(ops.xsdStr))

	}

	test("RDF namespace") {
		assertEquals(rdf.langString, URI(ops.xsdLangStr))
	}


end PrefixTest

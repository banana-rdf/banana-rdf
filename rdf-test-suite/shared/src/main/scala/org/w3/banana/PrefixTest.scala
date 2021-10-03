package org.w3.banana

import org.w3.banana.prefix.{FOAF, RDFPrefix, XSD}

open class PrefixTest[Rdf<:RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
	import ops.{given,*}
	import TestConstants.*
	val foaf = FOAF[Rdf]
	val xsd = XSD[Rdf]
	val rdf = RDFPrefix[Rdf]

	test("FOAF Prefix") {
		assert(foaf.age.===(URI(foafPre("age"))))
		assert(foaf.knows.===(URI(foafPre("knows"))))
		assert(foaf.homepage.===(URI(foafPre("homepage"))))
	}

	test("XSD Prefix") {
		assert(xsd.int === URI(xsdPre("int")))
		assert(xsd.integer === URI(xsdPre("integer")))
		assert(xsd.hexBinary === URI(xsdPre("hexBinary")))
		assert(xsd.string === URI(ops.xsdStr))
	}

	test("RDF namespace") {
		assert(rdf.langString === URI(ops.xsdLangStr))
	}


end PrefixTest

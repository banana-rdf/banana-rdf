package org.w3.banana

import org.w3.banana.RDF.*
import org.w3.banana.TestConstants.{bbl, tim}

open class StoreTest[Rdf<:RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
	import ops.{given,*}
	import org.w3.banana.prefix.{FOAF,XSD}

	val timbl: URI[Rdf] = URI(tim("i"))
	val bblf: URI[Rdf] = URI(bbl("i"))
	val timCard: URI[Rdf] = URI(tim(""))
	val bblfCard: URI[Rdf] = URI(bbl(""))
	val xsd: XSD[Rdf] = XSD[Rdf]
	val foaf: FOAF[Rdf] = FOAF[Rdf]

	val bkt = Quad(bblf,foaf.knows,timbl)
	val tnameq = Quad(timbl, foaf.name, "Tim"`@`Lang("en"))
	val bnameq = Quad(bblf, foaf.name, "Henry"`@`Lang("en"))

	test("default store works as a graph") {
		val store: Store[Rdf] = Store()
		store.add(bkt)
		val ci: Iterator[RDF.Quad[Rdf]] =  store.find(`*`,`*`,`*`,`*`)
		assertEquals(ci.toList,List(bkt))
		store.add(bkt)
		val ci1: Iterator[RDF.Quad[Rdf]] =  store.find(`*`,`*`,`*`,`*`)
		assertEquals(ci1.toList, List(bkt),"if we add the same quad to a store we don't get a bigger store")
		val ci2: Iterator[RDF.Quad[Rdf]] =  store.find(`*`,`*`,`*`,`*`)
		//let us see if this works. It may not be the most important
		assertEquals(ci2.toList,List(bkt),"if we add the same quad to a store we don't get a bigger store")
	}

	test("basic quads") {
		val store: Store[Rdf] = Store()
		val bktqSaysTim = bkt.at(timCard)
		store.add(bktqSaysTim)
		val ci =  store.find(`*`,`*`,`*`,`*`).toList
		assertEquals(ci,List(bktqSaysTim))
		val bktqSaysBbl = bkt.at(bblfCard)
		store.add(bktqSaysBbl)
		val ci2 =  store.find(`*`,`*`,`*`,`*`).toSet
		assertEquals(ci2,Set(bktqSaysBbl,bktqSaysTim),"we add two triples stated at different places and we get them back")
	}

end StoreTest

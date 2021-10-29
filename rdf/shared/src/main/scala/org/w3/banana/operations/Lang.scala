package org.w3.banana.operations

import org.w3.banana.RDF

trait Lang[Rdf<:RDF]:
	def apply(name: String): RDF.Lang[Rdf]
	def unapply(lang: RDF.Lang[Rdf]): Option[String] = Some(lang.label)
	extension (lang: RDF.Lang[Rdf])
		def label: String
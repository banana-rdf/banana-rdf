package org.w3.banana.operations

import org.w3.banana.RDF

trait BNode[Rdf<:RDF]:
	def apply(s: String): RDF.BNode[Rdf]
	def apply(): RDF.BNode[Rdf]
	extension (bn: RDF.BNode[Rdf])
		def label: String
end BNode
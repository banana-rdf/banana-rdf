package org.w3.banana.operations

import org.w3.banana.RDF
import org.w3.banana.RDF.rURI

trait rURI[Rdf<:RDF]:
	def apply(uriStr: String): RDF.rURI[Rdf]
	def asString(uri: RDF.rURI[Rdf]): String
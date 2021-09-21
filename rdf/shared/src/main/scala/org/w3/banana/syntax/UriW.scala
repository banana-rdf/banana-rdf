package org.w3.banana.syntax
import org.w3.banana.{RDF,Ops}
import RDF.*

extension [Rdf<:RDF](uri: URI[Rdf])(using ops: Ops[Rdf])
	def asString: String = ops.URI.asString(uri)
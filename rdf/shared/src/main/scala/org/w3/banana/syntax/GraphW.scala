package org.w3.banana.syntax

import org.w3.banana.{RDF,Ops}
import RDF.*

extension [Rdf<:RDF](graph: Graph[Rdf])(using ops: Ops[Rdf])
	def triples: Iterable[Triple[Rdf]] = ops.Graph.triplesIn(graph)


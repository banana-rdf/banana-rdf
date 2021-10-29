package org.w3.banana.operations

import org.w3.banana.RDF
import org.w3.banana.RDF.{rGraph, rTriple}

trait rGraph[Rdf<:RDF]:
	def empty: RDF.rGraph[Rdf]
	def apply(triples: Iterable[RDF.rTriple[Rdf]]): RDF.rGraph[Rdf]
	def apply(head: RDF.rTriple[Rdf], tail: RDF.rTriple[Rdf]*): RDF.rGraph[Rdf] =
		val it: Iterable[RDF.rTriple[Rdf]] = Iterable[RDF.rTriple[Rdf]](tail.prepended(head)*)
		apply(it)
	def triplesIn(graph: RDF.rGraph[Rdf]): Iterable[rTriple[Rdf]]
	def graphSize(graph: RDF.rGraph[Rdf]): Int
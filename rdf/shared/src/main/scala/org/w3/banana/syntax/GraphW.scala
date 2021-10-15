package org.w3.banana.syntax

import org.w3.banana.{RDF,Ops}
import RDF.*

//extension [Rdf<:RDF](graph: Graph[Rdf])(using ops: Ops[Rdf])
//	def contains(triple: Triple[Rdf]): Boolean = {
//		import ops.toConcreteNodeMatch
//		val (sub, rel, obj) = ops.Graph.fromTriple(triple)
//		select(sub,rel,obj).hasNext
//	}

extension [Rdf<:RDF](graph: rGraph[Rdf])(using ops: Ops[Rdf])
	def rtriples: Iterable[rTriple[Rdf]] = ops.rGraph.triplesIn(graph)
	def rsize: Int = ops.rGraph.graphSize(graph)


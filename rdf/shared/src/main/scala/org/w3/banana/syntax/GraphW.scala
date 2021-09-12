package org.w3.banana.syntax

import org.w3.banana.{RDF,Ops}
import RDF.*

extension [Rdf<:RDF](graph: Graph[Rdf])(using ops: Ops[Rdf])
	def triples: Iterable[Triple[Rdf]] = ops.Graph.triplesIn(graph)
	def union(otherGraph: Graph[Rdf]): Graph[Rdf] = ops.Graph.union(graph :: otherGraph :: Nil)
	def +(triple: Triple[Rdf]): Graph[Rdf] = ops.Graph.union(Seq(graph, ops.Graph(triple)))
	def diff(other: Graph[Rdf]): Graph[Rdf] = ops.Graph.diff(graph, other)
	def isIsomorphicWith(otherGraph: Graph[Rdf]): Boolean = ops.Graph.isomorphism(graph, otherGraph)
	def size: Int = ops.Graph.graphSize(graph)
//	def contains(triple: Triple[Rdf]): Boolean = {
//		import ops.toConcreteNodeMatch
//		val (sub, rel, obj) = ops.Graph.fromTriple(triple)
//		select(sub,rel,obj).hasNext
//	}



package org.w3.banana

import org.w3.banana.RDF

import RDF.*

trait PG[Rdf <: RDF](val pointer: Node[Rdf], val graph: Graph[Rdf])

object PG:
	def apply[Rdf <: RDF](pointer: Node[Rdf], graph: Graph[Rdf]): PG[Rdf] =
		new PG[Rdf](pointer, graph)

	def apply[Rdf <: RDF](node: Node[Rdf])(using rdf: Rdf): PG[Rdf] =
		PG[Rdf](node, rdf.Graph.empty)

	def unapply[Rdf <: RDF](pg: PG[Rdf]): Option[(Node[Rdf], Graph[Rdf])] =
		Some((pg.pointer, pg.graph))

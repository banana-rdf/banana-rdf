package org.w3.banana

import org.w3.banana.RDF

import RDF.*

class PG[Rdf <: RDFObj](val pointer: Node[Rdf], val graph: Graph[Rdf])

object PG:
//	def apply[Rdf <: RDFObj](pointer: Node[Rdf], graph: Graph[Rdf]): PG[Rdf] =
//		new PG[Rdf](pointer, graph)

	def apply[Rdf <: RDFObj](node: Node[Rdf])(using rdf: Rdf): PG[Rdf] =
		new PG[Rdf](node, rdf.Graph.empty.asInstanceOf[Graph[Rdf]])

	def unapply[Rdf <: RDFObj](pg: PG[Rdf]): Option[(Node[Rdf], Graph[Rdf])] =
		Some((pg.pointer, pg.graph))

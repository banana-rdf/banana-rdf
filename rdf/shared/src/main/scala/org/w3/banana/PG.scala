package org.w3.banana

import org.w3.banana.RDF

import RDF.*

class PG[Rdf <: RDFObj](val pointer: Node[Rdf], val graph: Graph[Rdf])

object PG:
	def apply[Rdf <: RDFObj](pointer: Node[Rdf], graph: Graph[Rdf]): PG[Rdf] =
		new PG[Rdf](pointer, graph)
	def apply[Rdf <: RDFObj](node: Node[Rdf])(using ops: Ops[Rdf]): PG[Rdf] =
		new PG[Rdf](node, ops.Graph.empty)

	def unapply[Rdf <: RDFObj](pg: PG[Rdf]): Option[(Node[Rdf], Graph[Rdf])] =
		Some((pg.pointer, pg.graph))

// this is what code would look like if one had to rely only on path dependent types:
//
//class PG2[Rdf <: RDFObj](using val rdf: Rdf)(val pointer: rdf.Node, val graph: rdf.Graph)
//
//object PG2:
//	def apply[Rdf <: RDFObj](using rdf: Rdf)(node: rdf.Node): PG2[Rdf] =
//		new PG2[Rdf]()(node, rdf.Graph.empty)
//
// thanks to neko-kai for coming up with the pattern matching method


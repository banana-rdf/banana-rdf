/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana

import RDF.*

class PG[Rdf <: RDF](val pointer: Node[Rdf], val graph: Graph[Rdf])

object PG:
   def apply[Rdf <: RDF](pointer: Node[Rdf], graph: Graph[Rdf]): PG[Rdf] =
     new PG[Rdf](pointer, graph)
   def apply[Rdf <: RDF](node: Node[Rdf])(using ops: Ops[Rdf]): PG[Rdf] =
     new PG[Rdf](node, ops.Graph.empty)

   def unapply[Rdf <: RDF](pg: PG[Rdf]): Option[(Node[Rdf], Graph[Rdf])] =
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

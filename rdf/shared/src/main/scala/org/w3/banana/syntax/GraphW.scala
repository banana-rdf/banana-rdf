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

package org.w3.banana.syntax

import org.w3.banana.{RDF, Ops}
import RDF.*

//extension [Rdf<:RDF](graph: Graph[Rdf])(using ops: Ops[Rdf])
//	def contains(triple: Triple[Rdf]): Boolean = {
//		import ops.toConcreteNodeMatch
//		val (sub, rel, obj) = ops.Graph.fromTriple(triple)
//		select(sub,rel,obj).hasNext
//	}

extension [Rdf <: RDF](graph: rGraph[Rdf])(using ops: Ops[Rdf])
   def rtriples: Iterable[rTriple[Rdf]] = ops.rGraph.triplesIn(graph)
   def rsize: Int                       = ops.rGraph.graphSize(graph)

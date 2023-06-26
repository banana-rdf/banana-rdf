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

import org.w3.banana.*
import RDF.*

case class PointedGraph[Rdf <: RDF](
    pointer: Node[Rdf],
    graph: Graph[Rdf]
)

object PointedGraph:
   def apply[R <: RDF](
       node: Node[R]
   )(using ops: Ops[R]): PointedGraph[R] =
     new PointedGraph[R](
       node,
       ops.Graph.empty
     )

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

package org.w3.banana.diesel
import org.w3.banana.{Ops, RDF}
import org.w3.banana.RDF.{rGraph, rNode, rURI, Literal, BNode, rStatement}

/** pointed Relative Graph, especially useful when constructing graphs usnig the DSL */
sealed trait PointedRGraph[R <: RDF]:
   type Pointer <: RDF.rStatement.Object[R]

   def pointer: Pointer
   def graph: rGraph[R]

sealed trait PointedSubjRGraph[R <: RDF] extends PointedRGraph[R]:
   type Pointer = rURI[R] | BNode[R]

object PointedSubjRGraph:
   def apply[R <: RDF](subj: rURI[R] | BNode[R])(using ops: Ops[R]): PointedSubjRGraph[R] =
     PointedSubjRGraph[R](subj, ops.rGraph.empty)

   def apply[R <: RDF](
       subj: rURI[R] | BNode[R],
       gr: rGraph[R]
   ): PointedSubjRGraph[R] =
     new PointedSubjRGraph[R]:
        override def pointer = subj
        override def graph = gr

sealed trait PointedLitRGraph[R <: RDF] extends PointedRGraph[R]:
   type Pointer = Literal[R]

object PointedLitRGraph:
   def apply[R <: RDF](
       pointr: Literal[R],
       rgraph: rGraph[R]
   ): PointedLitRGraph[R] =
     new PointedLitRGraph[R]:
        override def pointer = pointr
        override def graph = rgraph

   def apply[R <: RDF](lit: Literal[R])(using ops: Ops[R]): PointedLitRGraph[R] =
     PointedLitRGraph[R](lit, ops.rGraph.empty)

object PointedRGraph:
   def apply[R <: RDF](
       node: rNode[R]
   )(using ops: Ops[R]): PointedRGraph[R] =
     apply[R](node, ops.rGraph.empty)

   def apply[R <: RDF](
       node: rNode[R],
       graph: rGraph[R]
   )(using ops: Ops[R]): PointedRGraph[R] =
      import ops.given
      node match
       case bnodeTT(bnode) => PointedSubjRGraph[R](bnode, graph)
       case rUriTT(u)      => PointedSubjRGraph[R](u, graph)
       case literalTT(n)   => PointedLitRGraph[R](n, graph)
end PointedRGraph

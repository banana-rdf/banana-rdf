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

package org.w3.banana.operations

import io.lemonlabs.uri.AbsoluteUrl
import org.w3.banana.RDF
import org.w3.banana.RDF.{NodeAny, Triple, Statement as St}
import org.w3.banana.Ops

import scala.annotation.targetName

// todo: we may want a slightly richer notion of graph, namely one that
//     can keep track of namespaces and optional base.
//    - Jena keeps track of those
//    - Rdf4j ?
//    - other ?
trait Graph[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.given

   def empty: RDF.Graph[Rdf]
   def apply(triples: Iterable[RDF.Triple[Rdf]]): RDF.Graph[Rdf]
   def apply(head: RDF.Triple[Rdf], tail: RDF.Triple[Rdf]*): RDF.Graph[Rdf] =
      val it: Iterable[Triple[Rdf]] = Iterable[Triple[Rdf]](tail.prepended(head)*)
      apply(it)
   // todo: remove all the protected methods after moving code to extension.
   protected def triplesIn(graph: RDF.Graph[Rdf]): Iterable[RDF.Triple[Rdf]]
   protected def graphSize(graph: RDF.Graph[Rdf]): Int
   protected def gunion(graphs: Seq[RDF.Graph[Rdf]]): RDF.Graph[Rdf]
   protected def difference(g1: RDF.Graph[Rdf], g2: RDF.Graph[Rdf]): RDF.Graph[Rdf]
   protected def isomorphism(left: RDF.Graph[Rdf], right: RDF.Graph[Rdf]): Boolean
   protected def findTriples(
       graph: RDF.Graph[Rdf],
       s: St.Subject[Rdf] | RDF.NodeAny[Rdf],
       p: St.Relation[Rdf] | RDF.NodeAny[Rdf],
       o: St.Object[Rdf] | RDF.NodeAny[Rdf]
   ): Iterator[RDF.Triple[Rdf]]

   extension (graph: RDF.Graph[Rdf])
      @targetName("iso")
      infix def ≅(other: RDF.Graph[Rdf]): Boolean              = isomorphism(graph, other)
      infix def isomorphic(other: RDF.Graph[Rdf]): Boolean     = isomorphism(graph, other)
      def diff(other: RDF.Graph[Rdf]): RDF.Graph[Rdf]          = difference(graph, other)
      def size: Int                                            = graphSize(graph)
      def triples: Iterable[RDF.Triple[Rdf]]                   = triplesIn(graph)
      infix def union(graphs: RDF.Graph[Rdf]*): RDF.Graph[Rdf] = gunion(graph +: graphs)
      def +(triple: RDF.Triple[Rdf]): RDF.Graph[Rdf]           = gunion(Seq(graph, apply(triple)))
      def contains(t: RDF.Triple[Rdf]): Boolean                = find(t.subj, t.rel, t.obj).nonEmpty

      // todo: optimize by using info about what has changed, eg. if nothing return same
      def relativizeAgainst(base: AbsoluteUrl): RDF.rGraph[Rdf] =
        ops.rGraph(triples.map((t: RDF.Triple[Rdf]) => t.relativizeAgainst(base)._1))

      def find(
          subj: St.Subject[Rdf] | RDF.NodeAny[Rdf],
          rel: St.Relation[Rdf] | RDF.NodeAny[Rdf],
          obj: St.Object[Rdf] | RDF.NodeAny[Rdf]
      ): Iterator[RDF.Triple[Rdf]] = findTriples(graph, subj, rel, obj)

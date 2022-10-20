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

import io.lemonlabs.uri.{AbsoluteUrl, UrlWithScheme}
import org.w3.banana.{Ops, RDF}
import org.w3.banana.RDF.{rGraph, rTriple}

/** an rGraph is one that contains rTriples, which are triples that may contain relative urls.
  * Whereas one can take the union of two arbitrary RDF Graphs, taking the union of two rGraphs is
  * stating that they have the same context. As a result blank nodes from both grahs should not be
  * standardised apart (see
  * [[https://www.w3.org/TR/rdf11-mt/#shared-blank-nodes-unions-and-merges RDF1.1 semantics]]) Also
  * relative URLs are expected to have the same (unknown) base. Given this difference on the
  * treatment of blank nodes in the union we here choose to define the extension method `+ (t:
  * rTriple[Rdf])` instead.
  *
  * Relative Graphs are especially useful when constructing graphs, when one does not yet know where
  * it will be published. That decision of what the full context will may be one that has to be left
  * to the server, such as on a POST to an LDP Container (see
  * [[https://www.w3.org/TR/ldp/#dfn-ldp-server §5.2.3.7 of the LDP Spec]]). Resources with relative
  * URLs are also easier to write since one can create nodes like `<#i>` or `<foo/bar>`, that make
  * the relation between resources much easier to understand and to find.
  *
  * Would help if one had a notion of context for rGraphs that would allow them to be related, so
  * that one could not union two graphs that were wrongly related? One could also envisage that one
  * could specify some relations such as a relative path relation `rg1 r(../invoice/) rg2` allowing
  * them to be merged after all after a path adjustment (but this time with blank nodes standardised
  * apart) One could even reason with relative graphs then, so long as the context were fixed. This
  * seems possible, but it looks like a lot of work to think through the details.
  *
  * If we are never going to reason with these graphs, or even query them (which seems likely), then
  * it may be better to introduce a type Stream[rTriple] for constructing graphs, as that construct
  * does not care about standardising bnodes apart and avoids the then unnecessary cost of indexing
  * the triples. Such a construct would also be very useful when reading in graphs that just need to
  * be streamed to a DB. Note that such a stream could do with an optional context URI: if set we
  * have a stream leading to a normal RDF Graph, if not to a stream leading to the structure here.
  * This allows for DBs to perhaps keep track of the original structure of the document.
  *
  * Note that an rGraph will need to have an isomorphism test where a sequence could just compare
  * for triple equality of the sequence, because a graph has no order for the triples. Those are
  * needeed for tests essentially.
  */
trait rGraph[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.given
   def empty: RDF.rGraph[Rdf]
   def apply(triples: Iterable[RDF.rTriple[Rdf]]): RDF.rGraph[Rdf]
   def apply(head: RDF.rTriple[Rdf], tail: RDF.rTriple[Rdf]*): RDF.rGraph[Rdf] =
      val it: Iterable[RDF.rTriple[Rdf]] = Iterable[RDF.rTriple[Rdf]](tail.prepended(head)*)
      apply(it)

   extension (rGraph: RDF.rGraph[Rdf])
      def triples: Iterable[RDF.rTriple[Rdf]]
      def size: Int

      /** return a new graph with one triple added */
      infix def +(triple: RDF.rTriple[Rdf]): RDF.rGraph[Rdf] = rGraph ++ Seq(triple)

      /** Add a relative triple to the graph. Blank nodes are not standardised apart. Todo: could
        * one type blank nodes so that they are forced to belong to a graph?
        */
      infix def ++(triples: Seq[RDF.rTriple[Rdf]]): RDF.rGraph[Rdf]

      /** resolve the relative graph with a base URI
        *
        * todo: optimize by splitting triples into those that are changed and those that are not. If
        * nothing changed return original graph, or construct new graph - could be by removing
        * triples if less to remove than add....
        */
      def resolveAgainst(base: AbsoluteUrl): RDF.Graph[Rdf] =
        ops.Graph(triples.map((t: RDF.rTriple[Rdf]) => t.resolveAgainst(base)._1))

      /** A graph needs a complex isomorphism implementation just because there is no order to the
        * triples. In any case one must assume that both graphs are in the same context, ie have the
        * same base
        */
      infix def isomorphic(other: RDF.rGraph[Rdf]): Boolean

end rGraph

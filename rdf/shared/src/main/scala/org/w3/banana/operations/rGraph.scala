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
  * Whereas one can take the union of two RDF Graphs one can not take the union of two rGraphs, as
  * that could lead to name clashes: two relative URLs from two documents could be identical, yet
  * refer to different resources.
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

      /** resolve the relative graph with a base URI todo: optimize by splitting triples into those
        * that are changed and those that are not. If nothing changed return original graph, or
        * construct new graph - could be by removing triples if less to remove than add....
        */
      def resolveLenient(base: AbsoluteUrl): RDF.Graph[Rdf] =
        ops.Graph(triples.map((t: RDF.rTriple[Rdf]) => t.resolveLenient(base)._1))

end rGraph

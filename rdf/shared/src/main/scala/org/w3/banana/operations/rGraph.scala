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

import org.w3.banana.RDF
import org.w3.banana.RDF.{rGraph, rTriple}

trait rGraph[Rdf <: RDF]:
   def empty: RDF.rGraph[Rdf]
   def apply(triples: Iterable[RDF.rTriple[Rdf]]): RDF.rGraph[Rdf]
   def apply(head: RDF.rTriple[Rdf], tail: RDF.rTriple[Rdf]*): RDF.rGraph[Rdf] =
      val it: Iterable[RDF.rTriple[Rdf]] = Iterable[RDF.rTriple[Rdf]](tail.prepended(head)*)
      apply(it)
   def triplesIn(graph: RDF.rGraph[Rdf]): Iterable[rTriple[Rdf]]
   def graphSize(graph: RDF.rGraph[Rdf]): Int

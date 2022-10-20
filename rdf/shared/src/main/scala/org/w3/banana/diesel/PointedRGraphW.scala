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

import org.w3.banana.*
import org.w3.banana.RDF.*
//import org.w3.banana.binder._

import scala.util.*

/** Functions needed to **construct** a rGraph.
  *
  * Note this may be better build as a typeClass, since exactly the same methods make sense on an
  * `rGraph`, as on a `Stream[rTriple]`, and perhaps even on a Pointed DataSet (one may be adding
  * triples to a graph in a dataset...).
  *
  * We remove the search capabilities we had in banana 0.8.x and should add those to a different
  * extension, typeclass as those pragmatically require the triples to be indexed.
  */
class PointedRGraphW[Rdf <: RDF](val pointed: PointedSubjRGraph[Rdf]) extends AnyVal:

   import pointed.graph

   def a(clazz: rURI[Rdf])(using ops: Ops[Rdf]): PointedSubjRGraph[Rdf] =
      import ops.{given, *}
      val newGraph: RDF.rGraph[Rdf] = graph + rTriple(pointed.pointer, rdf.`type`, clazz)
      PointedSubjRGraph(pointed.pointer, newGraph)

   infix def --(p: rURI[Rdf]): PointedGraphPredicate[Rdf] =
     new PointedGraphPredicate[Rdf](pointed, p)

class PointedRGraphWInv[Rdf <: RDF](val pointed: PointedRGraph[Rdf]) extends AnyVal:

   infix def -<-(rel: rURI[Rdf])(using ops: Ops[Rdf]): PredicatePointedRGraph[Rdf] =
     new PredicatePointedRGraph[Rdf](rel, pointed)

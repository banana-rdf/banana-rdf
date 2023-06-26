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

import scala.language.implicitConversions

/** Needed for code like BNode() -- foaf.knows ->- tim */
given rNodeToPointedGraph[Rdf <: RDF](using
    Ops[Rdf]
): Conversion[RDF.rNode[Rdf], PointedRGraph[Rdf]] with
   def apply(node: RDF.rNode[Rdf]): PointedRGraph[Rdf] =
     PointedRGraph(node)

given literalToPointedGraph[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.Literal[Rdf], PointedRGraph[Rdf]] with
   def apply(node: RDF.Literal[Rdf]): PointedLitRGraph[Rdf] =
     PointedLitRGraph(node)

given subjToPointedGraphW[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.rStatement.Subject[Rdf], PointedRGraphW[Rdf]] with
   def apply(subj: RDF.rStatement.Subject[Rdf]): PointedRGraphW[Rdf] =
     new PointedRGraphW[Rdf](PointedSubjRGraph(subj))

given bNodeToPointedGraphW[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.BNode[Rdf], PointedRGraphW[Rdf]] with
   def apply(node: RDF.BNode[Rdf]): PointedRGraphW[Rdf] =
     new PointedRGraphW[Rdf](PointedSubjRGraph(node))

given rURIToPointedGraphW[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.rURI[Rdf], PointedRGraphW[Rdf]] with
   def apply(node: RDF.rURI[Rdf]): PointedRGraphW[Rdf] =
     new PointedRGraphW[Rdf](PointedSubjRGraph(node))

given rURIToPointedGraphWInv[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.rURI[Rdf], PointedRGraphWInv[Rdf]] with
   def apply(uri: RDF.rURI[Rdf]): PointedRGraphWInv[Rdf] =
     new PointedRGraphWInv[Rdf](PointedSubjRGraph(uri))

given literalToPointedGraphWInv[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.Literal[Rdf], PointedRGraphWInv[Rdf]] with
   def apply(literal: RDF.Literal[Rdf]): PointedRGraphWInv[Rdf] =
     new PointedRGraphWInv[Rdf](PointedLitRGraph(literal))

given bNodeToPointedGraphWInv[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.BNode[Rdf], PointedRGraphWInv[Rdf]] with
   def apply(bn: RDF.BNode[Rdf]): PointedRGraphWInv[Rdf] =
     new PointedRGraphWInv[Rdf](PointedSubjRGraph(bn))

given [Rdf <: RDF]: Conversion[PointedSubjRGraph[Rdf], PointedRGraphW[Rdf]] with
   def apply(pg: PointedSubjRGraph[Rdf]): PointedRGraphW[Rdf] =
     new PointedRGraphW[Rdf](pg)

given [Rdf <: RDF]: Conversion[PointedSubjRGraph[Rdf], PointedRGraphWInv[Rdf]] with
   def apply(pg: PointedSubjRGraph[Rdf]): PointedRGraphWInv[Rdf] =
     new PointedRGraphWInv[Rdf](pg)

given strToPG[Rdf <: RDF](using ops: Ops[Rdf]): Conversion[String, PointedRGraph[Rdf]] with
   def apply(str: String): PointedRGraph[Rdf] = PointedRGraph(ops.Literal(str))

//given nodeToPG[Rdf <: RDF](using ops: Ops[Rdf]): Conversion[RDF.Node[Rdf], PointedRGraph[Rdf]]
//  with
//   def apply(node: RDF.Node[Rdf]): PointedRGraph[Rdf] =
//      import ops.given
//      PointedRGraph(node)

extension [Rdf <: RDF](str: String)(using ops: Ops[Rdf])
  def lang(lang: RDF.Lang[Rdf]): RDF.Literal[Rdf] = ops.Literal(str, lang)

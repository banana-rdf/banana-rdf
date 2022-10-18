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

import org.w3.banana.{Ops, PointedRelGraph, RDF}
import scala.language.implicitConversions

/** Needed for code like BNode() -- foaf.knows ->- tim */
given rNodeToPointedGraph[Rdf <: RDF](using
    Ops[Rdf]
): Conversion[RDF.rNode[Rdf], PointedRelGraph[Rdf]] with
   def apply(node: RDF.rNode[Rdf]): PointedRelGraph[Rdf] =
     PointedRelGraph(node)

given literalToPointedGraph[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.Literal[Rdf], PointedRelGraph[Rdf]] with
   def apply(node: RDF.Literal[Rdf]): PointedRelGraph[Rdf] =
      import ops.given
      PointedRelGraph(node)

given rNodeToPointedGraphW[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.rNode[Rdf], PointedRelGraphW[Rdf]] with
   def apply(node: RDF.rNode[Rdf]): PointedRelGraphW[Rdf] =
     new PointedRelGraphW[Rdf](PointedRelGraph(node))

given bNodeToPointedGraphW[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.BNode[Rdf], PointedRelGraphW[Rdf]] with
   def apply(node: RDF.BNode[Rdf]): PointedRelGraphW[Rdf] =
      import ops.given
      new PointedRelGraphW[Rdf](PointedRelGraph(node))

given [Rdf <: RDF]: Conversion[PointedRelGraph[Rdf], PointedRelGraphW[Rdf]] with
   def apply(pg: PointedRelGraph[Rdf]): PointedRelGraphW[Rdf] =
     new PointedRelGraphW[Rdf](pg)

given strToPG[Rdf <: RDF](using ops: Ops[Rdf]): Conversion[String, PointedRelGraph[Rdf]] with
   import ops.given
   def apply(str: String): PointedRelGraph[Rdf] = PointedRelGraph(ops.Literal(str))

given nodeToPG[Rdf <: RDF](using ops: Ops[Rdf]): Conversion[RDF.Node[Rdf], PointedRelGraph[Rdf]]
  with
   def apply(node: RDF.Node[Rdf]): PointedRelGraph[Rdf] =
      import ops.given
      PointedRelGraph(node)

given nodeToPointedRelGraphW[Rdf <: RDF](using
    ops: Ops[Rdf]
): Conversion[RDF.Node[Rdf], PointedRelGraphW[Rdf]] with
   import ops.given
   def apply(node: RDF.Node[Rdf]): PointedRelGraphW[Rdf] =
     new PointedRelGraphW[Rdf](PointedRelGraph(node))

extension [Rdf <: RDF](str: String)(using ops: Ops[Rdf])
  def lang(lang: RDF.Lang[Rdf]): RDF.Literal[Rdf] = ops.Literal(str, lang)

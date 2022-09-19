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

import io.lemonlabs.uri.{AbsoluteUrl, RelativeUrl}
import org.w3.banana.{Ops, RDF}
import org.w3.banana.RDF.Statement as St

trait Triple[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.given

   type TripleI = (St.Subject[Rdf], St.Relation[Rdf], St.Object[Rdf])

   def apply(s: St.Subject[Rdf], p: St.Relation[Rdf], o: St.Object[Rdf]): RDF.Triple[Rdf]

   def unapply(t: RDF.Triple[Rdf]): Option[TripleI] = Some(untuple(t))

   def untuple(t: RDF.Triple[Rdf]): TripleI =
     (subjectOf(t), relationOf(t), objectOf(t))

   protected def subjectOf(s: RDF.Triple[Rdf]): St.Subject[Rdf]

   protected def relationOf(s: RDF.Triple[Rdf]): St.Relation[Rdf]

   protected def objectOf(s: RDF.Triple[Rdf]): St.Object[Rdf]

   extension (triple: RDF.Triple[Rdf])
      def subj: St.Subject[Rdf]               = subjectOf(triple)
      def rel: St.Relation[Rdf]               = relationOf(triple)
      def obj: St.Object[Rdf]                 = objectOf(triple)
      def at(g: St.Graph[Rdf]): RDF.Quad[Rdf] = ops.Quad(triple.subj, triple.rel, triple.obj, g)

      def relativizeAgainst(base: AbsoluteUrl): (RDF.rTriple[Rdf], Boolean) =
         val (sRz, sChg): (RDF.rStatement.Subject[Rdf],Boolean) = triple.subj.foldSubj(
           (u: RDF.URI[Rdf]) => u.relativizeAgainst(base),
           (bn: RDF.BNode[Rdf]) => (bn , false)
         )
         val (rRz, rChg): (RDF.rStatement.Relation[Rdf],Boolean) =
           triple.rel.asUri.relativizeAgainst(base)
         val (oRz, oChg): (RDF.rStatement.Object[Rdf],Boolean) =
            triple.obj.asNode.fold(
              uri => uri.relativizeAgainst(base),
              bn => (bn, false),
              lit => (lit, false)
            )
         if sChg || rChg || oChg
         then (ops.rTriple(sRz, rRz, oRz), true)
         else (triple.asInstanceOf[RDF.rTriple[Rdf]], false)
      end relativizeAgainst
      

   extension (rsubj: RDF.Statement.Subject[Rdf])
      // todo: find a way to remove this asInstanceOf
      def widenToNode: RDF.Node[Rdf] = rsubj.asInstanceOf[RDF.Node[Rdf]]
      def foldSubj[A](uriF: RDF.URI[Rdf] => A, bnF: RDF.BNode[Rdf] => A): A =
        rsubj match
           case uri: RDF.URI[Rdf]  => uriF(uri)
           case bn: RDF.BNode[Rdf] => bnF(bn)

   extension (rrel: RDF.Statement.Relation[Rdf])
     // todo: find a way to remove this asInstanceOf
     def asUri: RDF.URI[Rdf] = rrel.asInstanceOf[RDF.URI[Rdf]]

   extension (robj: RDF.Statement.Object[Rdf])
     // todo: find a way to remove this asInstanceOf
     def asNode: RDF.Node[Rdf] = robj.asInstanceOf[RDF.Node[Rdf]]

end Triple

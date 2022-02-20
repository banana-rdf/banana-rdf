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
import org.w3.banana.RDF.Statement as St

trait Triple[Rdf <: RDF](using ops: org.w3.banana.Ops[Rdf]):
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

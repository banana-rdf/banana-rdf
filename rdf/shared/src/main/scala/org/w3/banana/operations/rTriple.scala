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
import org.w3.banana.RDF.{rNode, rStatement, rTriple, rURI}

trait rTriple[Rdf <: RDF]:
   type rTripleI = (rNode[Rdf], rURI[Rdf], rNode[Rdf])
   import RDF.rStatement as rSt

   def apply(s: rSt.Subject[Rdf], p: rSt.Relation[Rdf], o: rSt.Object[Rdf]): RDF.rTriple[Rdf]
   def unapply(t: RDF.Triple[Rdf]): Option[rTripleI] = Some(untuple(t))
   def untuple(t: RDF.Triple[Rdf]): rTripleI
   protected def subjectOf(s: RDF.rTriple[Rdf]): rSt.Subject[Rdf]
   protected def relationOf(s: RDF.rTriple[Rdf]): rSt.Relation[Rdf]
   protected def objectOf(s: RDF.rTriple[Rdf]): rSt.Object[Rdf]
   // todo? should we only have the extension functions?
   extension (rtriple: RDF.rTriple[Rdf])
      def rsubj: rSt.Subject[Rdf] = subjectOf(rtriple)
      def rrel: rSt.Relation[Rdf] = relationOf(rtriple)
      def robj: rSt.Object[Rdf]   = objectOf(rtriple)
end rTriple

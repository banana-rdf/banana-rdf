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
import org.w3.banana.RDF.{rNode, rStatement, rTriple, rURI}
import org.w3.banana.{Ops, RDF}

/** An rTriple is potentially composed of relative URLs. As a result adding an rTriple to a graph
  * will return an rGraph. For the reason stated in rURI, we do not require an rTriple to contain an
  * rURI, because for Jena, rdf4j and other frameworks, that would require parsing each URI.
  * @tparam Rdf
  */
trait rTriple[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.given
   type rTripleI = (rNode[Rdf], rURI[Rdf], rNode[Rdf])
   import RDF.{Statement as St, rStatement as rSt}

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

      /** implementations built with clear relative URL types should optimise. resolve relative URLs
        * in triple with base. result._2 is true if a new object was created
        */
      def resolveLenient(base: AbsoluteUrl): (RDF.Triple[Rdf], Boolean) =
         val (s, sChange): (St.Subject[Rdf], Boolean) = rtriple.rsubj.fold(
           bn => (bn, false),
           uri => ops.rURI.resolveUri(uri, base).get
         )
         val (r, rChange): (St.Relation[Rdf], Boolean) = rtriple.rrel.asUri.resolveUrlLenient(base)
         val (o, oChange): (St.Object[Rdf], Boolean)   = rtriple.robj.asNode.resolveLenient(base)
         if sChange || rChange || oChange
         then (ops.Triple(s, r, o), true)
         else (rtriple.asInstanceOf[RDF.Triple[Rdf]], false)
      end resolveLenient

   extension (rsubj: RDF.rStatement.Subject[Rdf])
      // todo: find a way to remove this asInstanceOf
      def widenToNode: RDF.rNode[Rdf] = rsubj.asInstanceOf[RDF.rNode[Rdf]]
      def fold[A](bnF: RDF.BNode[Rdf] => A, uriF: RDF.rURI[Rdf] => A): A =
        rsubj match
           case bn: RDF.BNode[Rdf]  => bnF(bn)
           case rUri: RDF.rURI[Rdf] => uriF(rUri)

   extension (rrel: RDF.rStatement.Relation[Rdf])
     // todo: find a way to remove this asInstanceOf
     def asUri: RDF.rURI[Rdf] = rrel

   extension (robj: RDF.rStatement.Object[Rdf])
     // todo: find a way to remove this asInstanceOf
     def asNode: RDF.rNode[Rdf] = robj.asInstanceOf[RDF.rNode[Rdf]]
end rTriple

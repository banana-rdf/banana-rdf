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

package org.w3.banana.isomorphism

import org.w3.banana.Ops
import org.w3.banana.RDF

import scala.collection.immutable.ListMap
import scala.collection.{immutable, mutable}
import scala.util.Try

/*
 * The SimpleMappingGenerator implements only the first stage of Jeremy
 * Carroll's optimisation strategy. It classifies nodes only by the arrows
 * going in and out, but does not follow those further.
 *
 * @param VT the classifier
 * @param maxComplexity the maximum number of solutions to look at, otherwise fails
 */
final class SimpleMappingGenerator[R <: RDF](VT: () => VerticeCBuilder[R])(using ops: Ops[R])
    extends MappingGenerator[R]:

   import ops.{given, *}

   /** generate a list of possible bnode mappings, filtered by applying classification algorithm on
     * the nodes by relating them to other edges
     *
     * @return
     *   a ListMap mapping BNode from graph g1 to a smaller set of Bnodes from graph g2 which they
     *   should corresond to
     */
   def bnodeMappings(
       g1: RDF.Graph[R],
       g2: RDF.Graph[R]
   ): Try[immutable.ListMap[RDF.BNode[R], immutable.Set[RDF.BNode[R]]]] = Try {
     val clz1 = bnodeClassify(g1)
     val clz2 = bnodeClassify(g2)
     if clz1.size != clz2.size then
        throw ClassificationException(
          "the two graphs don't have the same number of classes.",
          clz1,
          clz2
        )
     val mappingOpts: mutable.Map[RDF.BNode[R], mutable.Set[RDF.BNode[R]]] =
       mutable.HashMap[RDF.BNode[R], mutable.Set[RDF.BNode[R]]]()
     for
        (vt, bnds1) <- clz1 // .sortBy { case (vt, bn) => bn.size }
        bnds2 <- clz2.get(vt)
     do
        if bnds2.size != bnds1.size then
           throw ClassificationException(
             s"the two graphs don't have the same number of bindings for type $vt",
             clz1,
             clz2
           )
        for bnd <- bnds1 do
           mappingOpts.get(bnd).orElse(Some(mutable.Set.empty[RDF.BNode[R]])).map { bnset =>
             mappingOpts.put(bnd, bnset ++= bnds2)
           }
     // todo: this transformation to immutable is expensive
     ListMap(mappingOpts.toSeq.map(l => (l._1, l._2.toSet)).sortBy(_._2.size)*)
   }

   /** This classification can be improved, but it is easier to debug while it is not so effective.
     *
     * @return
     *   a classification of bnodes by type, where nodes can only be matched by other nodes of the
     *   same type
     */
   def bnodeClassify(graph: RDF.Graph[R]): Map[VerticeClassification, Set[RDF.BNode[R]]] =
      val bnodeClass = mutable.HashMap[RDF.BNode[R], VerticeCBuilder[R]]()
      for tr <- graph.triples do
         tr.subj match
          case bn: RDF.BNode[R] =>
            val vt = bnodeClass.getOrElseUpdate(bn, VT())
            vt.setForwardRel(tr.rel, tr.obj)
          case _ => ()
         tr.obj match
          case bn: RDF.BNode[R] =>
            val vt = bnodeClass.getOrElseUpdate(bn, VT())
            vt.setBackwardRel(tr.rel, tr.subj)
          case _ => ()
      bnodeClass.view.mapValues(_.result).toMap
        .groupBy(_._2)
        .view.mapValues(_.keys.toSet).toMap
   end bnodeClassify

   case class ClassificationException(
       msg: String,
       clz1: Map[VerticeClassification, Set[RDF.BNode[R]]],
       clz2: Map[VerticeClassification, Set[RDF.BNode[R]]]
   ) extends MappingError(msg):
      override def toString() = s"ClassificationException($msg,$clz1,$clz2)"

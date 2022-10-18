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
//import org.w3.banana.binder._

class PointedGraphPredicate[Rdf <: RDF](pointed: PointedRelGraph[Rdf], p: RDF.rURI[Rdf]):

   infix def ->-(pointedObject: PointedRelGraph[Rdf])(using ops: Ops[Rdf]): PointedRelGraph[Rdf] =
      import ops.{given, *}
      import pointed.{graph as acc, pointer as s}
      import pointedObject.{graph as graphObject, pointer as o}
      val newGraph = acc ++ (rTriple(s, p, o) +: graphObject.triples.toSeq)
      PointedRelGraph(s, newGraph)

// For more general transformations we need ToPG
// let's first see how far we get without.

//  def ->-[T](o: T, os: T*)(using ops: Ops[Rdf], toPG: ToPG[Rdf, T]): PointedRelGraph[Rdf] = {
//    import ops._
//    if (os.isEmpty)
//      this.->-(toPG.toPG(o))
//    else
//      pointed -- p ->- ObjectList(o +: os)
//  }

//  def ->-[T](opt: Option[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = opt match {
//    case None => pointed
//    case Some(t) => this.->-(t)
//  }

//  def ->-[T](objList: ObjectList[T])(implicit ops: Ops[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = {
//    import ops._
//    objList.ts.foldLeft(pointed)(
//      (acc, t) => acc -- p ->- t
//    )
//  }
//
//  def ->-[T](objects: Set[T])(implicit ops: RDFOps[Rdf], toPG: ToPG[Rdf, T]): PointedGraph[Rdf] = {
//    import ops._
//    val graph = objects.foldLeft(pointed.graph) {
//      case (acc, obj) =>
//        val pg = toPG.toPG(obj)
//        ops.graphW(ops.graphW(acc) union Graph(Set(Triple(pointed.pointer, p, pg.pointer)))) union pg.graph
//    }
//    PointedGraph(pointed.pointer, graph)
//  }

end PointedGraphPredicate

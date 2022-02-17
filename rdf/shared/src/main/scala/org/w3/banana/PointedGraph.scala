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

//package org.w3.banana
//
//import org.w3.banana.*
//
//type RDFObj = RDF & Singleton
//
//trait RDFOps[T <: RDFObj](val rdf: T) {
//   def emptyGraph: rdf.Graph
//   def fromUri(uri: rdf.URI): String
//   def makeUri(s: String): rdf.URI
//}
//
//
//trait PointedGraph[T <: RDFObj](using val rdf: T) {
//  def pointer: rdf.Node
//  def graph: rdf.Graph
//}
//
//object PointedGraph {
//  def apply[T <: RDFObj](using rdf:T)(
//    node: rdf.Node,
//    inGraph: rdf.Graph
//  ): PointedGraph[rdf.type] =
//    new PointedGraph[rdf.type](){
//      val pointer = node
//      val graph = inGraph
//    }
//
//  def apply[T <: RDFObj](using rdf: T) (
//    node: rdf.Node
//  )(using ops: RDFOps[rdf.type]): PointedGraph[rdf.type] =
//    new PointedGraph[rdf.type]() {
//      val pointer = node
//      val graph   = ops.emptyGraph
//    }
//
//  def unapply[T <: RDFObj](using rdf: T)(
//    pg: PointedGraph[rdf.type]
//  )(using ops: RDFOps[T]): (rdf.Node, rdf.Graph) = (pg.pointer, pg.graph)
//
//}

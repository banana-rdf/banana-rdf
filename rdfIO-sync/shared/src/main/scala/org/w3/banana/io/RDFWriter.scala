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

package org.w3.banana
package io
import RDF.*

import _root_.io.lemonlabs.uri.AbsoluteUrl
import java.io.Writer
import cats.Functor
import cats.syntax.all.toFunctorOps

trait AbsoluteRDFWriter[Rdf <: RDF, M[_]: Functor, +S]:
   /** Write the triples of a graph to an java.io.Writer in a format that does not take relative URLs.
     */
   def write(triples: RDF.Graph[Rdf], os: Writer): M[Unit]

/**  a Writer that can serialise an RDF graph in a format that allows relative URLs  */
trait RDFWriter[Rdf <: RDF, M[_]: Functor, +T]:
   /** write out the Relative Graph to java.io.Writer Passing the graph as an
     * iterator of Triples allows one to specify the order of writing these out and also to
     * relativise any URIs to be written given the base
     */
   def write(
       graph: Graph[Rdf],
       wr: Writer,
       base: Option[AbsoluteUrl] = None,
       prefixes: Set[Prefix[Rdf]] = Set()
   ): M[Unit]
   
   def asString(
       graph: Graph[Rdf],
       base: Option[AbsoluteUrl] = None,
       prefixes: Set[Prefix[Rdf]] = Set()
   ): M[String] =
     val outs = java.io.StringWriter()
     write(graph, outs, base, prefixes).map(_ => outs.toString)
   
end RDFWriter

/** An Writer that can accept a relative Graph  for a format that allows relative URLs */
trait RDFrWriter[Rdf <:RDF, M[_]: Functor, +T]:
  
   /** write out the relative Graph to java.io.Writer Passing the graph as an
     * iterator of Triples allows one to specify the order of writing these out and also to
     * relativise any URIs to be written given the base. No need to specify the base, as we
     * assume the graph is already correctly set to relative.
     */
    def rgWrite(
       graph: rGraph[Rdf],
       wr: Writer,
       prefixes: Set[Prefix[Rdf]] = Set()
   ): M[Unit]
   
    def asString(
       graph: rGraph[Rdf],
       prefixes: Set[Prefix[Rdf]] = Set()
   ): M[String] =
     val outs = java.io.StringWriter()
     rgWrite(graph,outs, prefixes).map(_ => outs.toString)
   
end RDFrWriter


//    I don't think that existing implementations really provide output functions that
//    take Iterator[Triple] .
//    ( And it would not even be that great given that java.io is blocking )
//    But we can write our own such as NTriplesWriter
//


/** serialise an RDF Graph into a syntax S that does not admit relative URLs.
  * @tparam Rdf
  *   RDF encoding
  * @tparam M
  *   Context in which result is wropped
  * @tparam S
  *   Syntax phantom marker trait
  */
trait AbsoluteRDFIterWriter[Rdf <: RDF, M[_], +S]:
   /** Write the triples of a graph to an java.io.Writer
     * note: this Iterator output would be very nice to have, but do existing implementations even provide this capability?
     * note: (nice to have apart from the fact that java.io.* is blocking)
     */
   def write(triples: Iterator[Triple[Rdf]], os: Writer): M[Unit]

/** Serialise an RDF Graph into a syntax S that accepts relative URIs
  * @tparam Rdf
  * @tparam M
  * @tparam T
  */
trait RDFIterWriter[Rdf <: RDF, M[_], +T]: // extends Writer[Rdf#Graph,M,T] {
   /** write out the Relative Triples from a graph to wr: java.io.Writer Passing the graph as an
     * iterator of Triples allows one to specify the order of writing these out and also to
     * relativise any URIs to be written
     * note: this Iterator output would be very nice to have, but do existing implementations even provide this capability?
     * note: (nice to have apart from the fact that java.io.* is blocking)
     */
   def write(
       graph: Iterator[rTriple[Rdf]],
       wr: Writer,
       prefixes: Set[Prefix[Rdf]] = Set()
   ): M[Unit]

//todo: an rdf writer that outputs relative URLs correctly

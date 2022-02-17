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

import java.io.Writer

/** serialise an RDF Graph into a syntax S that does not admit relative URLs.
  * @tparam Rdf
  *   RDF encoding
  * @tparam M
  *   Context in which result is wropped
  * @tparam S
  *   Syntax phantom marker trait
  */
trait AbsoluteRDFWriter[Rdf <: RDF, M[_], +S]:
   /** Write the triples of a graph to an java.io.Writer
     */
   def write(triples: Iterator[Triple[Rdf]], os: Writer): M[Unit]

/** Serialise an RDF Graph into a syntax S that accepts relative URIs
  * @tparam Rdf
  * @tparam M
  * @tparam T
  */
trait RDFWriter[Rdf <: RDF, M[_], +T]: // extends Writer[Rdf#Graph,M,T] {
   /** write out the Reltaive Triples from a graph to wr: java.io.Writer Passing the graph as an
     * interator of Triples allows one to specify the order of writing these out and also to
     * relativise any URIs to be written
     */
   def write(
       graph: Iterator[rTriple[Rdf]],
       wr: Writer,
       prefixes: Set[Prefix[Rdf]] = Set()
   ): M[Unit]

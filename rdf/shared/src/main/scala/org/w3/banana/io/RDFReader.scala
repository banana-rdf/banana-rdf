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

import java.io.*
import RDF.*

/** Reader for Syntaxes that contain no relative URLs.
  * @tparam Rdf
  * @tparam M
  * @tparam S
  */
trait AbsoluteRDFReader[Rdf <: RDF, M[_], +S]:
   /** todo: look into returning an Iterator rather than an M[Graph]. Iterators would be better for
     * memory useage, and in any case leave it open to use this to produce a Graph. Parses an rdf
     * Graph from an java.io.InputStream returning the value in context M (Try, Future, ...)
     *
     * Note: Many libraries prefer to have an intput stream that allows the parser to switch
     * encoding and syntax for when the mime type was wrong. But that indicates that those should be
     * called before this.
     */
   // def read(is: InputStream): M[Graph[Rdf]]

   /** Parses an RDF Graph from a java.io.Reader and a base URI.
     * @param base
     *   the base URI to use, to resolve relative URLs
     */
   def read(reader: Reader): M[Graph[Rdf]]

/** RDF readers for syntaxes that can contain relative URLs
  *
  * All functions return results in the context `M`. `S` is a phantom type for the syntax.
  */
trait RDFReader[Rdf <: RDF, M[_], +S]:

   /** Parses an RDF Graph from an java.io.InputStream
     * @param base
     *   URI in context M (Try, Future, ...)
     */
   def read(is: InputStream, base: String): M[Graph[Rdf]]

   /** Parses an RDF Graph from a java.io.Reader and a base URI.
     * @param base
     *   the base URI to use, to resolve relative URLs
     */
   def read(reader: Reader, base: String): M[Graph[Rdf]]

/** traits to be implemented by Syntaxes S that can contain relative URLs, for Rdf implementations
  * that can return relative graphs.
  *
  * @tparam Rdf
  *   types to use
  * @tparam M
  *   context in which result is returned
  * @tparam S
  *   syntax phantom type
  */
trait RelRDFReader[Rdf <: RDF, M[_], +S]:

   /** Tries parsing an RDF Graph from an java.io.InputStream
     */
   def read(is: InputStream): M[rGraph[Rdf]]

   /** Tries parsing an RDF Graph from a java.io.Reader and a base URI.
     * @param base
     *   the base URI to use, to resolve relative URLs found in the InputStream
     */
   def read(reader: Reader): M[rGraph[Rdf]]

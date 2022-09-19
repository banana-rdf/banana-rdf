/*
 *  Copyright (c) 2016  W3C Members
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

package org.w3.banana.io

import org.w3.banana.{RDF, Ops}
import RDF.*

import java.io.Writer

import scala.util.Try

/** Generic NTriplesWriter
  * @param ops
  *   implicit Rdf operations, that by default are resolved from Rdf typeclass
  * @tparam Rdf
  *   class with Rdf types
  */
class NTriplesWriter[Rdf <: RDF](using val ops: Ops[Rdf])
    extends AbsoluteRDFWriter[Rdf, Try, NTriples]:

   import ops.{given, *}

   protected def tripleAsString(t: Triple[Rdf]): String =
     node2Str(t.subj) + " " + node2Str(t.rel) + " " + node2Str(t.obj) + " ."

   /** Translates node to its ntriples string representation
     * @param node
     *   Rdf node
     * @return
     */
   def node2Str(node: RDF.Node[Rdf]): String = node.fold(
     (url: RDF.URI[Rdf]) => "<" + url.value + ">",
     (bn: RDF.BNode[Rdf]) => "_:" + bn.label,
     (lit: RDF.Literal[Rdf]) =>
       lit.fold(
         txt => "\"" + txt + "\"",
         (txt, lang) => "\"" + txt + "\"" + "@" + lang.label,
         (txt, tp) => "\"" + txt + "\"" + "^^<" + tp.value + ">"
       )
   )

   override def write(graph: Iterator[Triple[Rdf]], wr: Writer): Try[Unit] = Try {
     for triple <- graph do
        val line = tripleAsString(triple) + "\r\n"
        wr.write(line)
   }

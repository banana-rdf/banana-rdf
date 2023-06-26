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

package org.w3.banana.jena.io

import io.lemonlabs.uri.AbsoluteUrl
import org.apache.commons.io.input.ReaderInputStream
import org.apache.jena.graph.{Factory, NodeFactory, Node as JenaNode, Triple as JenaTriple}
import org.apache.jena.irix.IRIxResolver
import org.apache.jena.riot.*
import org.apache.jena.riot.system.*
import org.apache.jena.sparql.core.Quad
import org.w3.banana.RDF.{Graph, rGraph}
import org.w3.banana.io.*
import org.w3.banana.jena.JenaRdf
import org.w3.banana.jena.JenaRdf.R

import java.io.*
import java.nio.charset.Charset
import scala.util.*

object JenaRDFReader:
   type RDFReaderT[S] = RDFReader[R, Try, S]
   type RelReaderT[S] = RelRDFReader[R, Try, S]

   def makeRDFReader[S](lang: Lang): RDFReaderT[S] =
     new RDFReader[R, Try, S]:

        import JenaRdf.ops

        def read(is: InputStream, base: AbsoluteUrl): Try[Graph[R]] = Try {
          import scala.language.unsafeNulls
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph
          val builder: RDFParserBuilder = RDFParser.create()
          builder.source(is)
            .lang(lang)
            .base(base.toString)
            .parse(graph)
          graph.asInstanceOf[Graph[R]]
        }

        def read(reader: Reader, base: AbsoluteUrl): Try[Graph[R]] = Try {
          // why is Jena deprecating Readers, which should be the correct level to parse character based documents?
          import scala.language.unsafeNulls
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph()
          val builder: RDFParserBuilder = RDFParser.create()
          builder.source(reader)
            .lang(lang)
            .base(base.toString)
            .parse(graph)
          graph.asInstanceOf[Graph[R]]
        }
   end makeRDFReader

   def makeRDFRelReader[S](lang: Lang): RelReaderT[S] =
     new RelRDFReader[R, Try, S]:
        def read(is: InputStream): Try[rGraph[R]] = Try {
          import scala.language.unsafeNulls
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph()
          val builder: RDFParserBuilder = RDFParser.create()
          import org.apache.jena.riot.RelURIParserBuilder
          val ir: IRIxResolver = IRIxResolver.create()
            .noBase().resolve(false)
            .allowRelative(true).build()
          builder.source(is)
            .lang(lang)
            .resolveURIs(false)
            .strict(false)
            .checking(false)
            .resolver(ir)
            .parse(graph)
          graph.asInstanceOf[rGraph[R]]
        }

        def read(reader: Reader): Try[rGraph[R]] =
          // transforming a reader into an input stream in order then to parse it as chars, is really horrible
          read(new ReaderInputStream(reader, Charset.forName("utf-8")))

   given rdfxmlReader: RDFReaderT[RDFXML] = makeRDFReader[RDFXML](Lang.RDFXML.nn)
   given rdfxmlRelReader: RelReaderT[RDFXML] = makeRDFRelReader[RDFXML](Lang.RDFXML.nn)

   given turtleReader: RDFReaderT[Turtle] = makeRDFReader[Turtle](Lang.TURTLE.nn)
   given turtleRelReader: RelReaderT[Turtle] = makeRDFRelReader[Turtle](Lang.TURTLE.nn)

   // given n3Reader: RDFReaders[N3] = makeRDFReader[N3](Lang.N3.nn)
   /** Jena uses [[https://github.com/filip26/titanium-json-ld Titanium Parser]] This needs to fetch
     * resources on the web. (In its own threads!)
     */
   given jsonLdReader: RDFReaderT[JsonLdCompacted] =
     makeRDFReader[JsonLdCompacted](Lang.JSONLD11.nn)
   given jsonLdRelReader: RelReaderT[JsonLdCompacted] =
     makeRDFRelReader[JsonLdCompacted](Lang.JSONLD11.nn)

end JenaRDFReader

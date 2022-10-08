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

package org.w3.banana.jena
package io

import _root_.io.lemonlabs.uri.AbsoluteUrl
import org.apache.jena.graph as jenaTp
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.impl.RDFWriterFImpl
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Abbrev
import org.apache.jena.riot.{Lang as JenaLang, RDFWriter as RioWriter, *}
import org.apache.jena.shared.PrefixMapping
import org.w3.banana.io.*
import org.w3.banana.{Prefix, RDF}
import cats.Functor
import com.apicatalog.jsonld.{JsonLd, JsonLdOptions}
import com.apicatalog.jsonld.api.CompactionApi
import com.apicatalog.jsonld.document.{Document, JsonDocument, RdfDocument}
import com.apicatalog.jsonld.lang.Keywords
import com.apicatalog.jsonld.processor.FromRdfProcessor
import com.apicatalog.rdf.RdfDataset
import jakarta.json.stream.JsonGenerator
import jakarta.json.{
  Json,
  JsonArray,
  JsonObject,
  JsonObjectBuilder,
  JsonStructure,
  JsonWriterFactory
}
import org.apache.jena.riot.system.JenaTitanium
import org.apache.jena.sparql.core.DatasetGraph
import org.apache.jena.sparql.util.Context

import java.io.*
import java.util
import java.util.Map
import scala.util.*

/** Helpers to create Jena writers. */
object JenaRDFWriter:

   import org.w3.banana.jena.JenaRdf.*
   import JenaRdf.Jena
   import JenaRdf.ops.{*, given}
   type RDFWriterTry[T] = RDFWriter[Jena, Try, T] & RDFrWriter[Jena, Try, T]

   given rdfxmlWriter: RDFWriter[Jena, Try, RDFXML] with
      def write(
          graph: RDF.Graph[Jena],
          wr: Writer,
          base: Option[AbsoluteUrl],
          prefixes: Set[Prefix[Jena]] = Set()
      ): Try[Unit] = Try {
        val writer = new RDFXML_Abbrev()
        if base.isDefined then writer.setProperty("relativeURIs", "same-document,relative")
        val model =
          ModelFactory.createModelForGraph(graph.asInstanceOf[org.apache.jena.graph.Graph])
        for pre <- prefixes do writer.setNsPrefix(pre.prefixName, pre.prefixIri.value)
        writer.write(model, wr, base.map(_.toString).getOrElse(null))
      }
   end rdfxmlWriter

   given rdfxmlRelWriter: RDFrWriter[Jena, Try, RDFXML] with
      def rgWrite(
          graph: RDF.rGraph[Jena],
          wr: Writer,
          prefixes: Set[Prefix[Jena]] = Set()
      ): Try[Unit] = Try {
        val writer = new RDFXML_Abbrev()
        writer.setProperty("relativeURIs", "same-document,relative")
        val model =
          ModelFactory.createModelForGraph(graph.asInstanceOf[org.apache.jena.graph.Graph])
        for pre <- prefixes do writer.setNsPrefix(pre.prefixName, pre.prefixIri.value)
        writer.write(model, wr, null)
      }
   end rdfxmlRelWriter

   given turtleWriter: RDFWriter[Jena, Try, Turtle] =
     makeRDFWriter(RDFFormat.TURTLE_PRETTY.nn)(builder => builder.set(RIOT.symTurtleOmitBase, true))

   given turtleRelWriter: RDFrWriter[Jena, Try, Turtle] =
     makeRDFRelWriter(RDFFormat.TURTLE_PRETTY.nn)

// I doubt these are really N3 Writers. They would need something supporting rules
//   given n3Writer: RDFWriter[Jena,Try,N3] = makeRDFWriter[N3](RDFLanguages.N3.nn)
//   given n3RelWriter: RDFrWriter[Jena,Try,N3] = makeRDFRelWriter[N3](RDFLanguages.N3.nn)

   /** this does not work for JSON-LD as Jena uses
     * [[https://github.com/filip26/titanium-json-ld/ Titanium JSON-LD]] (see
     * org.apache.jena.riot.writer.JsonLD11Writer, org.apache.jena.riot.lang.LangJSONLD11)
     * JsonLD11Writer drops the base!
     *
     * which creates its own RDF class hierarchy, and it does not accept relative URLs. Translating
     * to titanium throws an exception. But we may as well let caller be aware of this limitation.
     * todo: one could create a fake base URL, translate rGraphs to Graph using that and then send
     * to Titanium with the base set to that given jsonldCompactedRelWriter: RDFrWriter[Jena, Try,
     * JsonLdCompacted] = makeRDFRelWriter[JsonLdCompacted](RDFFormat.JSONLD11_PRETTY.nn)
     */
   given jsonldCompactedWriter: RDFWriter[Jena, Try, JsonLdCompacted] =
     // does not work because JsonLD11Writer does not implement base
     //     makeRDFWriter[JsonLdCompacted](RDFFormat.JSONLD11_PRETTY.nn)()
     new RDFWriter[Jena, Try, JsonLdCompacted]:
        def write(
            graph: RDF.Graph[Jena],
            wr: Writer,
            base: Option[AbsoluteUrl],
            prefixes: Set[Prefix[Jena]]
        ): Try[Unit] = Try {
          // taken from Jena 4.6.1 org.apache.jena.riot.writer.JsonLD11Writer.writePretty
          import scala.language.unsafeNulls

          val options = new JsonLdOptions()
          options.setCompactToRelative(true)
          val g: org.apache.jena.graph.Graph = graph.asInstanceOf[org.apache.jena.graph.Graph]
          val dsg: DatasetGraph              = org.apache.jena.sparql.core.DatasetGraphOne.create(g)
          val ds: RdfDataset                 = JenaTitanium.convert(dsg)
          val doc: Document                  = RdfDocument.of(ds)
          // Native types.
          // This looses information -- xsd:int becomes xsd:integer, xsd:double becomes xsd:decimal
          //   options.setUseNativeTypes(true);
          val array: JsonArray = FromRdfProcessor.fromRdf(doc, options)

          // Build context
          val cxt = Json.createObjectBuilder
          // Do not add @version. JSON-LD 1.0 processors would reject any input even if it is OK for JSON-LD 1.0.
          // cxt.add(Keywords.VERSION, "1.1");
          prefixes.foreach { (px: Prefix[Jena]) =>
            if !px.prefixName.isEmpty then cxt.add(px.prefixName, px.prefixIri.value)
          }
          val vocab = dsg.prefixes.get("")
          if vocab != null then cxt.add(Keywords.VOCAB, vocab)

          val context = cxt.build()

          // Object to write.
          val writeRdf = Json.createObjectBuilder
            .add(Keywords.CONTEXT, context)
            .add(Keywords.GRAPH, array)
            .build
          val contextDoc: JsonDocument = JsonDocument.of(context)

          // Compaction.
          val api: CompactionApi = JsonLd.compact(JsonDocument.of(writeRdf), contextDoc)
            .options(options)
            .base(base.map(_.toJavaURI).getOrElse(null))
//          api.rdfStar()
          //        // Non-absolute URIs.
          //        if ( dsg.prefixes().containsPrefix("") )
          //            api.base(dsg.prefixes().get(""));
          // JSON Object to output - JSON array
          val w: JsonObject              = api.get()
          val config                     = util.Map.of(JsonGenerator.PRETTY_PRINTING, true)
          val factory: JsonWriterFactory = Json.createWriterFactory(config)
          val jsonWriter                 = factory.createWriter(wr)
          jsonWriter.write(w)
          wr.write("\n")
          wr.flush()
        }
        end write
   end jsonldCompactedWriter

   private[JenaRDFWriter] def makeRDFWriter[S](format: RDFFormat)(
       setBuilder: RDFWriterBuilder => Unit = _ => ()
   ): RDFWriter[Jena, Try, S] =
     new RDFWriter[Jena, Try, S]:
        def write(
            graph: RDF.Graph[Jena],
            wr: Writer,
            base: Option[AbsoluteUrl],
            prefixes: Set[Prefix[Jena]]
        ): Try[Unit] = Try {
          import scala.language.unsafeNulls
          val jg: jenaTp.Graph    = graph.asInstanceOf[jenaTp.Graph]
          val pmap: PrefixMapping = jg.getPrefixMapping
          pmap.clearNsPrefixMap()
          for p <- prefixes do pmap.setNsPrefix(p.prefixName, p.prefixIri.value)
          val wrb: RDFWriterBuilder = RioWriter.source(jg)
          wrb.format(format)
            .base(base.map(_.toString).getOrElse(null))
          setBuilder(wrb)
          val rioWr: RioWriter = wrb.build()
          rioWr.output(wr)
        }
   end makeRDFWriter

   private[JenaRDFWriter] def makeRDFRelWriter[S](format: RDFFormat): RDFrWriter[Jena, Try, S] =
     new RDFrWriter[Jena, Try, S]:
        def rgWrite(
            graph: RDF.rGraph[Jena],
            wr: Writer,
            prefixes: Set[Prefix[Jena]]
        ): Try[Unit] = Try {
          import scala.language.unsafeNulls
          val jg: jenaTp.Graph = graph.asInstanceOf[jenaTp.Graph]
          val pmap             = jg.getPrefixMapping
          for p <- prefixes do pmap.setNsPrefix(p.prefixName, p.prefixIri.value)
          val wrb: RDFWriterBuilder = RioWriter.source(jg)
          wrb.format(format)
          val rioWr: RioWriter = wrb.build()
          rioWr.output(wr)
        }
   end makeRDFRelWriter

end JenaRDFWriter

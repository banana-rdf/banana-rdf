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
import org.apache.jena.sparql.util.Context

import java.io.*
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

   given turtleWriter: RDFWriter[Jena, Try, Turtle]  = makeRDFWriter(RDFFormat.TURTLE_PRETTY.nn)(builder => builder.set(RIOT.symTurtleOmitBase, true))


   given turtleRelWriter: RDFrWriter[Jena, Try, Turtle] = makeRDFRelWriter(RDFFormat.TURTLE_PRETTY.nn)
   
// I doubt these are really N3 Writers. They would need something supporting rules
//   given n3Writer: RDFWriter[Jena,Try,N3] = makeRDFWriter[N3](RDFLanguages.N3.nn)
//   given n3RelWriter: RDFrWriter[Jena,Try,N3] = makeRDFRelWriter[N3](RDFLanguages.N3.nn)

   given jsonldCompactedWriter: RDFWriter[Jena, Try, JsonLdCompacted] =
     makeRDFWriter[JsonLdCompacted](RDFFormat.JSONLD11_PRETTY.nn)()


  // this does not work. Jena uses [[https://github.com/filip26/titanium-json-ld/ Titanium JSON-LD]] which
  // creates its own RDF class hierarchy, and it does not accept relative URLs. Translating to
  // titanium throws an excpetion. But we may as well let caller be aware of this limitation.
  // todo: one could create a fake base URL, translate rGraphs to Graph using that and then send to Titanium with
  //       the base set to that
  // given jsonldCompactedRelWriter: RDFrWriter[Jena, Try, JsonLdCompacted] =
  //   makeRDFRelWriter[JsonLdCompacted](RDFFormat.JSONLD11_PRETTY.nn)

   private[JenaRDFWriter]
   def makeRDFWriter[S](format: RDFFormat)(setBuilder: RDFWriterBuilder => Unit = _ => ()): RDFWriter[Jena, Try, S] =
     new RDFWriter[Jena, Try, S]:
        def write(
            graph: RDF.Graph[Jena],
            wr: Writer,
            base: Option[AbsoluteUrl],
            prefixes: Set[Prefix[Jena]]
        ): Try[Unit] = Try {
          import scala.language.unsafeNulls
          val jg: jenaTp.Graph = graph.asInstanceOf[jenaTp.Graph]
          val pmap = jg.getPrefixMapping
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

   private[JenaRDFWriter]
   def makeRDFRelWriter[S](format: RDFFormat): RDFrWriter[Jena, Try, S] =
    new RDFrWriter[Jena, Try, S]:
        def rgWrite(
            graph: RDF.rGraph[Jena],
            wr: Writer,
            prefixes: Set[Prefix[Jena]]
        ): Try[Unit] = Try {
          import scala.language.unsafeNulls
          val jg: jenaTp.Graph = graph.asInstanceOf[jenaTp.Graph]
          val pmap = jg.getPrefixMapping
          for p <- prefixes do pmap.setNsPrefix(p.prefixName, p.prefixIri.value)
          val wrb: RDFWriterBuilder = RioWriter.source(jg)
          wrb.format(format)
          val rioWr: RioWriter = wrb.build()
          rioWr.output(wr)
        }
   end makeRDFRelWriter

end JenaRDFWriter

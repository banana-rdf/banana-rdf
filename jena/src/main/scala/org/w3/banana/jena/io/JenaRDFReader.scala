package org.w3.banana.jena
package io

import org.apache.jena.graph.{Node => JenaNode, Triple => JenaTriple, _}
import org.apache.jena.rdf.model.{RDFReader => _}
import org.apache.jena.riot.RDFParser

import java.io._
import org.apache.jena.riot._
import org.apache.jena.riot.system._
import org.w3.banana.io._
import org.w3.banana.jena.{ Jena, JenaOps }
import scala.util._
import java.nio.charset.Charset
import org.apache.commons.io.input.ReaderInputStream
import java.nio.charset.StandardCharsets

/** A triple sink that accumulates triples in a graph. */
final class TripleSink(implicit ops: JenaOps) extends StreamRDF {

  var prefixes: Map[String, String] = Map.empty
  val graph: Jena#Graph = Factory.createDefaultGraph

  def base(base: String): Unit = ()
  def finish(): Unit = ()
  def prefix(prefix: String, iri: String): Unit = prefixes += (prefix -> iri)
  def quad(quad: org.apache.jena.sparql.core.Quad): Unit = ()
  def start(): Unit = ()
  def triple(triple: JenaTriple): Unit = {
    def isXsdString(node: JenaNode): Boolean = {
      node.isLiteral &&
        node.getLiteralDatatypeURI == "http://www.w3.org/2001/XMLSchema#string"
    }
    val o = triple.getObject
    val t =
      // if o is a xsd:string literal
      if (isXsdString(o)) {
        // then replace the object by a clean "plain" literal without the xsd:string
        new JenaTriple(
          triple.getSubject,
          triple.getPredicate,
          NodeFactory.createLiteral(o.getLiteralLexicalForm.toString, null, null)
        )
      } else {
        // otherwise everything is fine
        triple
      }
    graph.add(t)
  }

  def tuple(tuple: org.apache.jena.atlas.lib.tuple.Tuple[JenaNode]): Unit = ()
}

object JenaRDFReader {

  def makeRDFReader[S](ops: JenaOps, lang: Lang): RDFReader[Jena, Try, S] = new RDFReader[Jena, Try, S] {
    // NOTE: There is also forceLang(lang)
    val factory = RDFParser.create().lang(lang)

    def read(is: InputStream, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink
      factory.base(base).source(is).build().parse(sink)
      sink.graph
    }

    def read(reader: Reader, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink
      val cs: Charset = StandardCharsets.UTF_8
      val is = new ReaderInputStream(reader, cs)
      factory.base(base).source(is).build().parse(sink)
      sink.graph
    }
  }

  def makeRDFLoader() = new RDFLoader[Jena, Try] {
    /**
     * Read triples from the given location.
     *
     * The syntax is determined from input source URI
     *  (content negotiation or extension).
     */
    def load(url: java.net.URL): Try[Jena#Graph] = {
      Try{
        RDFDataMgr.loadGraph(url.toString)
//    	  val sink = new TripleSink
//        RDFParser.create().source(url.toExternalForm()).parse(sink)
//        sink.graph
      }
    }
  }

  implicit def rdfxmlReader()(implicit ops: JenaOps): RDFReader[Jena, Try, RDFXML] = makeRDFReader[RDFXML](ops, Lang.RDFXML)

  implicit def turtleReader()(implicit ops: JenaOps): RDFReader[Jena, Try, Turtle] = makeRDFReader[Turtle](ops, Lang.TURTLE)

  implicit def n3Reader()(implicit ops: JenaOps): RDFReader[Jena, Try, N3] = makeRDFReader[N3](ops, Lang.N3)

  implicit def jsonldReader()(implicit ops: JenaOps): RDFReader[Jena, Try, JsonLd] = makeRDFReader[JsonLd](ops, Lang.JSONLD)

  implicit def rdfLoader()(implicit ops: JenaOps): RDFLoader[Jena, Try] = makeRDFLoader()
}

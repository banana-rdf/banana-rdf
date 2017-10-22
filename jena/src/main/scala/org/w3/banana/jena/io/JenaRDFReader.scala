package org.w3.banana.jena.io

import org.apache.jena.graph.{Node => JenaNode, Triple => JenaTriple, _}
import java.io._

import org.apache.jena.riot._
import org.apache.jena.riot.lang.{PipedQuadsStream, PipedRDFIterator}
import org.apache.jena.riot.system._
import org.apache.jena.sparql.core.Quad
import org.w3.banana.io._
import org.w3.banana.jena.{Jena, JenaOps}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util._

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
    def isXsdString(node: JenaNode): Boolean =
      node.isLiteral &&
      node.getLiteralDatatypeURI == "http://www.w3.org/2001/XMLSchema#string"
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

private class JenaRDFQuadReader[S](lang: Lang) extends RDFQuadReader[Jena, Try, S] {

  val factory = RDFParserRegistry.getFactory(lang)

  private def prepareQuadIterator(in: InputStream, base: String): PipedRDFIterator[Quad] = {
    val iter = new PipedRDFIterator[Quad]()
    factory.create(RDFLanguages.TRIG).read(in, base, null, new PipedQuadsStream(iter), null)
    iter
  }
  private def prepareQuadIterator(reader: Reader, base: String): PipedRDFIterator[Quad] = {
    val iter = new PipedRDFIterator[Quad]()
    factory.create(RDFLanguages.TRIG).read(reader, base, null, new PipedQuadsStream(iter), null)
    iter
  }

  def read(in: InputStream, base: String): Try[Jena#Graph] = Try {
    val iter = prepareQuadIterator(in, base)
    val graph: Jena#Graph = Factory.createDefaultGraph

    for(quad <- iter.asScala) {
      graph.add(quad.asTriple())
    }
    graph
  }

  def read(reader: Reader, base: String): Try[Jena#Graph] = Try {
    val iter = prepareQuadIterator(reader, base)
    val graph: Jena#Graph = Factory.createDefaultGraph

    for(quad <- iter.asScala) {
      graph.add(quad.asTriple())
    }
    graph
  }

  def readAll(is: InputStream, base: String) = Try {
    val iter = prepareQuadIterator(is, base)
    val graphMap = mutable.Map.empty[Option[Jena#Node], Jena#Graph]

    for(quad <- iter.asScala) {
      val context = Option(quad.getGraph)
      if(!graphMap.contains(context)) {
        val graph = Factory.createDefaultGraph()
        graph.add(quad.asTriple())
        graphMap += (context -> graph)
      } else {
        graphMap(context).add(quad.asTriple())
      }
    }
    graphMap.toMap

  }

  def readAll(reader: Reader, base: String) = Try {
    val iter = prepareQuadIterator(reader, base)
    val graphMap = mutable.Map.empty[Option[Jena#Node], Jena#Graph]

    for(quad <- iter.asScala) {
      val context = Option(quad.getGraph)
      if(!graphMap.contains(context)) {
        val graph = Factory.createDefaultGraph()
        graph.add(quad.asTriple())
        graphMap += (context -> graph)
      } else {
        graphMap(context).add(quad.asTriple())
      }
    }
    graphMap.toMap
  }

  def read(is: InputStream, base: String, graphName: Node_URI) = Try {
    val iter = prepareQuadIterator(is, base)
    val graph: Jena#Graph = Factory.createDefaultGraph

    for(quad <- iter.asScala) {
      if(quad.getGraph == graphName) {
        graph.add(quad.asTriple())
      }
    }
    graph
  }

  def read(reader: Reader, base: String, graphName: Node_URI) = Try {
    val iter = prepareQuadIterator(reader, base)
    val graph: Jena#Graph = Factory.createDefaultGraph

    for(quad <- iter.asScala) {
      if(quad.getGraph == graphName) {
        graph.add(quad.asTriple())
      }
    }
    graph
  }

  def readDefaultGraph(is: InputStream, base: String) = Try {
    val iter = prepareQuadIterator(is, base)
    val graph: Jena#Graph = Factory.createDefaultGraph

    for(quad <- iter.asScala) {
      if(quad.isDefaultGraph  ) {
        graph.add(quad.asTriple())
      }
    }
    graph
  }

  def readDefaultGraph(reader: Reader, base: String) = Try {
    val iter = prepareQuadIterator(reader, base)
    val graph: Jena#Graph = Factory.createDefaultGraph

    for(quad <- iter.asScala) {
      if(quad.getGraph == Quad.tripleInQuad || quad.isDefaultGraph) {
        graph.add(quad.asTriple())
      }
    }
    graph
  }

}

object JenaRDFReader {

  def makeRDFQuadReader[S](ops: JenaOps, lang: Lang): RDFQuadReader[Jena, Try, S] = new JenaRDFQuadReader[S](lang)


  def makeRDFReader[S](ops: JenaOps, lang: Lang): RDFReader[Jena, Try, S] = new RDFReader[Jena, Try, S] {
    val factory = RDFParserRegistry.getFactory(lang)

    def read(is: InputStream, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink
      factory.create(lang).read(is, base, null, sink, null)
      //      RDFDataMgr.parse(sink, is, base, lang)
      sink.graph
    }

    def read(reader: Reader, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink
      RDFDataMgr.parse(sink, reader.asInstanceOf[StringReader], base, lang)
      sink.graph
    }
  }

  implicit def rdfxmlReader()(implicit ops: JenaOps): RDFReader[Jena, Try, RDFXML] = makeRDFReader[RDFXML](ops, Lang.RDFXML)

  implicit def trigReader()(implicit ops: JenaOps): RDFQuadReader[Jena, Try, TriG] = makeRDFQuadReader[TriG](ops, Lang.TRIG)

  implicit def turtleReader()(implicit ops: JenaOps): RDFReader[Jena, Try, Turtle] = makeRDFReader[Turtle](ops, Lang.TURTLE)

  implicit def n3Reader()(implicit ops: JenaOps): RDFReader[Jena, Try, N3] = makeRDFReader[N3](ops, Lang.N3)

}

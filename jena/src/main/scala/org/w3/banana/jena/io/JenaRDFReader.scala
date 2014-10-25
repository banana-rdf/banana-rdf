package org.w3.banana.jena.io

import com.hp.hpl.jena.graph.{Node => JenaNode, Triple => JenaTriple, _}
import com.hp.hpl.jena.rdf.model.{RDFReader => _}
import java.io._
import org.apache.jena.riot._
import org.apache.jena.riot.system._
import org.w3.banana.io._
import org.w3.banana.jena.{Jena, JenaOps}
import scala.util._

/** A triple sink that accumulates triples in a graph. */
final class TripleSink(implicit ops: JenaOps) extends StreamRDF {

  var prefixes: Map[String, String] = Map.empty
  val graph: Jena#Graph = Factory.createDefaultGraph

  def base(base: String): Unit = ()
  def finish(): Unit = ()
  def prefix(prefix: String, iri: String): Unit = prefixes += (prefix -> iri)
  def quad(quad: com.hp.hpl.jena.sparql.core.Quad): Unit = ()
  def start(): Unit = ()
  def triple(triple: JenaTriple): Unit = {
    def isXsdString(node: JenaNode): Boolean =
      node.isLiteral &&
        node.getLiteralDatatypeURI == "http://www.w3.org/2001/XMLSchema#string"
    val o = triple.getObject
    val t =
      // if o is a xsd:string literal
      if (isXsdString(o))
        // then replace the object by a clean "plain" literal without the xsd:string
        new JenaTriple(
          triple.getSubject,
          triple.getPredicate,
          NodeFactory.createLiteral(o.getLiteralLexicalForm.toString, null, null))
      else
        // otherwise everything is fine
        triple
    graph.add(t)
  }
  def tuple(tuple: org.apache.jena.atlas.lib.Tuple[JenaNode]): Unit = ()
}

object JenaRDFReader {

  def makeRDFReader[S](ops: JenaOps, lang: Lang)(implicit _syntax:  Syntax[S]): RDFReader[Jena, S] = new RDFReader[Jena, S] {

    val factory = RDFParserRegistry.getFactory(lang)

    def read(is: InputStream, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink
      factory.create(lang).read(is, base, null, sink, null)
      //      RDFDataMgr.parse(sink, is, base, lang)
      sink.graph
    }

    def read(reader: Reader, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink
      RDFDataMgr.parse(sink, reader, base, lang)
      sink.graph
    }
  }

  implicit def rdfxmlReader()(implicit ops: JenaOps): RDFReader[Jena, RDFXML] = makeRDFReader[RDFXML](ops, Lang.RDFXML)

  implicit def turtleReader()(implicit ops: JenaOps): RDFReader[Jena, Turtle] = makeRDFReader[Turtle](ops, Lang.TURTLE)

  implicit def n3Reader()(implicit ops: JenaOps): RDFReader[Jena, N3] = makeRDFReader[N3](ops, Lang.N3)

  implicit val selector: ReaderSelector[Jena] =
    ReaderSelector[Jena, RDFXML] combineWith ReaderSelector[Jena, Turtle]

}

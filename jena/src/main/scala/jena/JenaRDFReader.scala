package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model.{ RDFReader => _, _ }
import com.hp.hpl.jena.graph.{ Triple => JenaTriple, Node => JenaNode, _ }
import scala.util._
import org.apache.jena.riot._
import org.apache.jena.riot.system._

class TripleSink(ops: JenaOpsSpecifics) extends StreamRDF {

  var triples: Set[Jena#Triple] = Set.empty
  var prefixes: Map[String, String] = Map.empty
  def graph: Jena#Graph = ops.makeGraph(triples)

  def base(base: String): Unit = ()
  def finish(): Unit = ()
  def prefix(prefix: String, iri: String): Unit = prefixes += (prefix -> iri)
  def quad(quad: com.hp.hpl.jena.sparql.core.Quad): Unit = ()
  def start(): Unit = ()
  def triple(triple: JenaTriple): Unit = {
    def isPlainLiteral(node: JenaNode): Boolean =
      node.isLiteral &&                     // it's really a literal
      node.getLiteralDatatypeURI == null && // not intended to be a typed literal
      node.getLiteralLanguage.isEmpty       // and not a lang literal either
    val o = triple.getObject
    val t =
      // if o is a plain literal
      if (isPlainLiteral(o))
        // then replace the object by a clean typed literal with an xsd:string
        new JenaTriple(
          triple.getSubject,
          triple.getPredicate,
          NodeFactory.createLiteral(o.getLiteralLexicalForm.toString, null, ops.xsdString))
      else
        // otherwise everything is fine
        triple
    triples += t
  }
  def tuple(tuple: org.apache.jena.atlas.lib.Tuple[JenaNode]): Unit = ()
}

object JenaRDFReader {

  def makeRDFReader[S](ops: JenaOpsSpecifics, lang: Lang)(implicit _syntax: Syntax[S]): RDFReader[Jena, S] = new RDFReader[Jena, S] {
    val syntax = _syntax
    def read(is: InputStream, base: String): Try[Jena#Graph] = Try {
      val sink = new TripleSink(ops)
      RDFDataMgr.parse(sink, is, base, lang)
      sink.graph
    }
  }

  implicit def rdfxmlReader(ops: JenaOpsSpecifics): RDFReader[Jena, RDFXML] = makeRDFReader[RDFXML](ops, Lang.RDFXML)

  implicit def turtleReader(ops: JenaOpsSpecifics): RDFReader[Jena, Turtle] = makeRDFReader[Turtle](ops, Lang.TURTLE)

  implicit val selector: ReaderSelector[Jena] = 
    ReaderSelector[Jena, RDFXML] combineWith ReaderSelector[Jena, Turtle]

}

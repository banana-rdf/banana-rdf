package org.w3.banana.bigdata

import java.util.{Locale, NoSuchElementException}

import com.bigdata.rdf.model._
import org.openrdf.model.Resource
import org.openrdf.model.impl._
import org.w3.banana.bigdata.Bigdata
import org.w3.banana.isomorphism.{GraphIsomorphism, SimpleMappingGenerator, VerticeCBuilder}
import org.w3.banana.{DefaultURIOps, RDFOps}

class BigdataOps(implicit config:BigdataConfig[Bigdata]) extends RDFOps[Bigdata] with DefaultURIOps[Bigdata] with BigdataMGraphOps
{

  lazy val valueFactory: BigdataValueFactory= BigdataValueFactoryImpl.getInstance(config.basePrefix)

  def emptyGraph: Bigdata#Graph = BigdataGraph(Map.empty,0)

  def makeGraph(triples: Iterable[Bigdata#Triple]): Bigdata#Graph =    triples.foldLeft(emptyGraph) { (g, triple) => g + triple }

  def getTriples(graph: Bigdata#Graph): Iterable[Bigdata#Triple] = graph.triples

  // triple

  def makeTriple(s: Bigdata#Node, p: Bigdata#URI, o: Bigdata#Node): Bigdata#Triple =
    s match {
      case res:Resource=>  valueFactory.createStatement(res,p,o)

      case _=> throw new RuntimeException("makeTriple: in Sesame subject " + p.toString + " must be a either URI or BlankNode")
    }

  def fromTriple(t: Bigdata#Triple): (Bigdata#Node, Bigdata#URI, Bigdata#Node) =  (t.getSubject, t.getPredicate, t.getObject)

  def foldNode[T](node: Bigdata#Node)(funURI: Bigdata#URI => T, funBNode: Bigdata#BNode => T, funLiteral: Bigdata#Literal => T): T = node match {
    case iri: Bigdata#URI => funURI(iri)
    case bnode: Bigdata#BNode => funBNode(bnode)
    case literal: Bigdata#Literal => funLiteral(literal)
  }

  // URI


  def makeUri(iriStr: String): Bigdata#URI =valueFactory.createURI(iriStr)

  def fromUri(node: Bigdata#URI): String = node.toString

  // bnode

  def makeBNode() = valueFactory.createBNode()

  def makeBNodeLabel(label: String): Bigdata#BNode =valueFactory.createBNode(label)

  def fromBNode(bn: Bigdata#BNode): String = bn.getID

  // literal

  val __xsdString = makeUri("http://www.w3.org/2001/XMLSchema#string")
  val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  class LangLiteral(label: String, language: String) extends LiteralImpl(label, language) {
    this.setDatatype(__rdfLangString)
  }

  def makeLiteral(lexicalForm: String, datatype: Bigdata#URI): Bigdata#Literal =
  valueFactory.createLiteral(lexicalForm,datatype)


  def makeLangTaggedLiteral(lexicalForm: String, lang: Bigdata#Lang): Bigdata#Literal =
  {
    valueFactory.createLiteral(lexicalForm,lang)
  }

  def fromLiteral(literal: Bigdata#Literal): (String, Bigdata#URI, Option[Bigdata#Lang]) =
    (literal.getLabel, literal.getDatatype, Option(literal.getLanguage))

  /**
   *  language tags are cases insensitive according to
   * <a href="http://tools.ietf.org/html/bcp47#section-2.1.1">RFC 5646: Tags for Identifying Languages</a>
   * which is referenced by <a href="http://www.w3.org/TR/rdf11-concepts/#section-Graph-Literal">RDF11 Concepts</a>.
   * Sesame does not take this into account, so canonicalise here to lower case. ( The NTriples Tests don't pass
   * if the `.toLowerCase` transformation is removed .
   */
  def makeLang(langString: String): Bigdata#Lang = langString.toLowerCase(Locale.ENGLISH)

  def fromLang(lang: Bigdata#Lang): String = lang

  // graph traversal

  val ANY: Bigdata#NodeAny = null

  implicit def toConcreteNodeMatch(node: Bigdata#Node): Bigdata#NodeMatch = node.asInstanceOf[Bigdata#Node]

  def foldNodeMatch[T](nodeMatch: Bigdata#NodeMatch)(funANY: => T, funConcrete: Bigdata#Node => T): T =
    if (nodeMatch == null)
      funANY
    else
      funConcrete(nodeMatch.asInstanceOf[Bigdata#Node])

  def find(graph: Bigdata#Graph, subject: Bigdata#NodeMatch, predicate: Bigdata#NodeMatch, objectt: Bigdata#NodeMatch): Iterator[Bigdata#Triple] =
    graph.find(subject, predicate, objectt).iterator

  // graph union

  def union(graphs: Seq[Bigdata#Graph]): Bigdata#Graph =     graphs.foldLeft(Graph.empty) { (g1, g2) => g1.union(g2) }

  def diff(g1: Bigdata#Graph, g2: Bigdata#Graph): Bigdata#Graph = {
    @annotation.tailrec
    def loop(g: Bigdata#Graph, triples: Iterator[Bigdata#Triple]): Bigdata#Graph = {
      if (triples.hasNext) {
        val triple = triples.next()
        loop(
          try {
            g.removeExistingTriple(triple)
          } catch {
            case e: NoSuchElementException => g
          },
          triples)
      } else {
        g
      }
    }
    loop(g1, g2.triples.iterator)
  }
  // graph isomorphism

  // graph isomorphism
  lazy val iso = new GraphIsomorphism[Bigdata](new SimpleMappingGenerator[Bigdata](VerticeCBuilder.simpleHash[Bigdata])) //TODO: fix null error

  def isomorphism(left: Bigdata#Graph, right: Bigdata#Graph): Boolean = {
    iso.findAnswer(left, right).isSuccess
  }

  def graphSize(g: Bigdata#Graph): Int = g.size

}

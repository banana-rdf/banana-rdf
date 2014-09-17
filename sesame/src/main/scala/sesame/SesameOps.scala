package org.w3.banana.sesame

import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.model.util._
import org.w3.banana._

import scala.collection.JavaConverters._

class SesameOps extends RDFOps[Sesame] with DefaultURIOps[Sesame] {

  val valueFactory: ValueFactory = ValueFactoryImpl.getInstance()

  // graph

  def emptyGraph: Sesame#Graph = new LinkedHashModel

  def makeGraph(it: Iterable[Sesame#Triple]): Sesame#Graph = {
    val graph = new LinkedHashModel
    it foreach { t => graph add t }
    graph
  }

  def getTriples(graph: Sesame#Graph): Iterable[Sesame#Triple] = graph.asScala

  // triple

  def makeTriple(s: Sesame#Node, p: Sesame#URI, o: Sesame#Node): Sesame#Triple = new StatementImpl(s.asInstanceOf[Resource], p, o)

  def fromTriple(t: Sesame#Triple): (Sesame#Node, Sesame#URI, Sesame#Node) = {
    val s = t.getSubject
    val p = t.getPredicate
    val o = t.getObject
    if (p.isInstanceOf[Sesame#URI])
      (s, p.asInstanceOf[Sesame#URI], o)
    else
      throw new RuntimeException("fromTriple: predicate " + p.toString + " must be a URI")
  }

  // node

  def foldNode[T](node: Sesame#Node)(funURI: Sesame#URI => T, funBNode: Sesame#BNode => T, funLiteral: Sesame#Literal => T): T = node match {
    case iri: Sesame#URI => funURI(iri)
    case bnode: Sesame#BNode => funBNode(bnode)
    case literal: Sesame#Literal => funLiteral(literal)
  }

  // URI

  /**
   * we provide our own builder for Sesame#URI to relax the constraint "the URI must be absolute"
   * this constraint becomes relevant only when you add the URI to a Sesame store
   */
  def makeUri(iriStr: String): Sesame#URI = {
    try {
      new URIImpl(iriStr)
    } catch {
      case iae: IllegalArgumentException =>
        new URI {
          override def equals(o: Any): Boolean = o.isInstanceOf[URI] && o.asInstanceOf[URI].toString == iriStr
          def getLocalName: String = iriStr
          def getNamespace: String = ""
          override def hashCode: Int = iriStr.hashCode
          override def toString: String = iriStr
          def stringValue: String = iriStr
        }
    }

  }

  def fromUri(node: Sesame#URI): String = node.toString

  // bnode

  def makeBNode() = valueFactory.createBNode()

  def makeBNodeLabel(label: String): Sesame#BNode = new BNodeImpl(label)

  def fromBNode(bn: Sesame#BNode): String = bn.getID

  // literal

  val __xsdString = makeUri("http://www.w3.org/2001/XMLSchema#string")
  val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  class LangLiteral(label: String, language: String) extends LiteralImpl(label, language) {
    this.setDatatype(__rdfLangString)
  }

  def makeLiteral(lexicalForm: String, datatype: Sesame#URI): Sesame#Literal =
    new LiteralImpl(lexicalForm, datatype)

  def makeLangTaggedLiteral(lexicalForm: String, lang: Sesame#Lang): Sesame#Literal =
    new LangLiteral(lexicalForm, lang)

  def fromLiteral(literal: Sesame#Literal): (String, Sesame#URI, Option[Sesame#Lang]) =
    (literal.getLabel, literal.getDatatype, Option(literal.getLanguage))

  // lang

  def makeLang(langString: String): Sesame#Lang = langString

  def fromLang(lang: Sesame#Lang): String = lang

  // graph traversal

  val ANY: Sesame#NodeAny = null

  implicit def toConcreteNodeMatch(node: Sesame#Node): Sesame#NodeMatch = node.asInstanceOf[Sesame#Node]

  def foldNodeMatch[T](nodeMatch: Sesame#NodeMatch)(funANY: => T, funConcrete: Sesame#Node => T): T =
    if (nodeMatch == null)
      funANY
    else
      funConcrete(nodeMatch.asInstanceOf[Sesame#Node])

  def find(graph: Sesame#Graph, subject: Sesame#NodeMatch, predicate: Sesame#NodeMatch, objectt: Sesame#NodeMatch): Iterator[Sesame#Triple] = {
    def sOpt: Option[Resource] =
      if (subject == null)
        Some(null)
      else
        foldNode(subject)(Some.apply, Some.apply, _ => None)
    def pOpt: Option[Sesame#URI] =
      if (predicate == null)
        Some(null)
      else
        foldNode(predicate)(Some.apply, _ => None, _ => None)
    val r = for {
      s <- sOpt
      p <- pOpt
    } yield {
      graph.filter(s, p, objectt).iterator.asScala
    }
    r getOrElse Iterator.empty
  }

  // graph union

  def union(graphs: Seq[Sesame#Graph]): Sesame#Graph = {
    graphs match {
      case Seq(x) => x
      case _ =>
        val graph = new LinkedHashModel
        graphs.foreach(g => getTriples(g) foreach { triple => graph.add(triple) })
        graph
    }
  }

  def diff(g1: Sesame#Graph, g2: Sesame#Graph): Sesame#Graph = {
    val graph = new LinkedHashModel
    getTriples(g1) foreach { triple =>
      if (!g2.contains(triple)) graph add triple
    }
    graph
  }

  // graph isomorphism

  /**
   * the new ModelUtil.equals changed its semantics. See
   * - https://openrdf.atlassian.net/browse/SES-1695
   * - https://groups.google.com/forum/#!topic/sesame-devel/CGFDn7mESLg/discussion
   */
  def isomorphism(left: Sesame#Graph, right: Sesame#Graph): Boolean = {
    val leftNoContext = left.asScala.map(s => makeTriple(s.getSubject, s.getPredicate, s.getObject)).asJava
    val rightNoContext = right.asScala.map(s => makeTriple(s.getSubject, s.getPredicate, s.getObject)).asJava
    ModelUtil.equals(leftNoContext, rightNoContext)
  }

  def graphSize(g: Sesame#Graph): Int = g.size()

}

package org.w3.banana.rdf4j

import java.util.Locale

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl._
import org.eclipse.rdf4j.model.util._
import org.w3.banana._

import scala.collection.JavaConverters._

class Rdf4jOps extends RDFOps[Rdf4j] with Rdf4jMGraphOps with DefaultURIOps[Rdf4j] {

  val valueFactory: ValueFactory = SimpleValueFactory.getInstance()

  // graph

  def emptyGraph: Rdf4j#Graph = new LinkedHashModel

  def makeGraph(it: Iterable[Rdf4j#Triple]): Rdf4j#Graph = {
    val graph = new LinkedHashModel
    it foreach { t => graph add t }
    graph
  }

  def getTriples(graph: Rdf4j#Graph): Iterable[Rdf4j#Triple] = graph.asScala

  // triple

  def makeTriple(s: Rdf4j#Node, p: Rdf4j#URI, o: Rdf4j#Node): Rdf4j#Triple =
    s match {
      case res: Resource => valueFactory.createStatement(res, p, o)
      case _=> throw new RuntimeException("makeTriple: in RDF4J, subject " + p.toString + " must be a either URI or BlankNode")
    }

  def fromTriple(t: Rdf4j#Triple): (Rdf4j#Node, Rdf4j#URI, Rdf4j#Node) =  (t.getSubject, t.getPredicate, t.getObject)

  // node

  def foldNode[T](node: Rdf4j#Node)(funURI: Rdf4j#URI => T, funBNode: Rdf4j#BNode => T, funLiteral: Rdf4j#Literal => T): T = node match {
    case iri: Rdf4j#URI => funURI(iri)
    case bnode: Rdf4j#BNode => funBNode(bnode)
    case literal: Rdf4j#Literal => funLiteral(literal)
  }

  // URI

  /**
   * we provide our own builder for Rdf4j#URI to relax the constraint "the URI must be absolute"
   * this constraint becomes relevant only when you add the URI to a Sesame store
   */
  def makeUri(iriStr: String): Rdf4j#URI = {
    try {
      valueFactory.createIRI(iriStr)
    } catch {
      case iae: IllegalArgumentException =>
        new IRI {
          override def equals(o: Any): Boolean = o.isInstanceOf[IRI] && o.asInstanceOf[IRI].toString.equals(iriStr)
          def getLocalName: String = iriStr
          def getNamespace: String = ""
          override def hashCode: Int = iriStr.hashCode
          override def toString: String = iriStr
          def stringValue: String = iriStr
        }
    }
  }

  def fromUri(node: Rdf4j#URI): String = node.toString

  // bnode

  def makeBNode() = valueFactory.createBNode()

  def makeBNodeLabel(label: String): Rdf4j#BNode = valueFactory.createBNode(label)

  def fromBNode(bn: Rdf4j#BNode): String = bn.getID

  // literal

  val __xsdString = makeUri("http://www.w3.org/2001/XMLSchema#string")

  def makeLiteral(lexicalForm: String, datatype: Rdf4j#URI): Rdf4j#Literal =
    valueFactory.createLiteral(lexicalForm, datatype)

  def makeLangTaggedLiteral(lexicalForm: String, lang: Rdf4j#Lang): Rdf4j#Literal =
    // By setting the language, RDF4J sets the langString data type implicitly
    valueFactory.createLiteral(lexicalForm, lang)

  def fromLiteral(literal: Rdf4j#Literal): (String, Rdf4j#URI, Option[Rdf4j#Lang]) =
    (literal.getLabel, literal.getDatatype, Option(literal.getLanguage.orElse(null)))
  
  def makeLang(langString: String): Rdf4j#Lang = langString

  def fromLang(lang: Rdf4j#Lang): String = lang

  // graph traversal

  val ANY: Rdf4j#NodeAny = null

  implicit def toConcreteNodeMatch(node: Rdf4j#Node): Rdf4j#NodeMatch = node.asInstanceOf[Rdf4j#Node]

  def foldNodeMatch[T](nodeMatch: Rdf4j#NodeMatch)(funANY: => T, funConcrete: Rdf4j#Node => T): T =
    if (nodeMatch == null)
      funANY
    else
      funConcrete(nodeMatch.asInstanceOf[Rdf4j#Node])

  def find(graph: Rdf4j#Graph, subject: Rdf4j#NodeMatch, predicate: Rdf4j#NodeMatch, objectt: Rdf4j#NodeMatch): Iterator[Rdf4j#Triple] = {
    def sOpt: Option[Resource] =
      if (subject == null)
        Some(null)
      else
        foldNode(subject)(Some.apply, Some.apply, _ => None)
    def pOpt: Option[Rdf4j#URI] =
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

  def union(graphs: Seq[Rdf4j#Graph]): Rdf4j#Graph = {
    graphs match {
      case Seq(x) => x
      case _ =>
        val graph = new LinkedHashModel
        graphs.foreach(g => getTriples(g) foreach { triple => graph.add(triple) })
        graph
    }
  }

  def diff(g1: Rdf4j#Graph, g2: Rdf4j#Graph): Rdf4j#Graph = {
    val graph = new LinkedHashModel
    getTriples(g1) foreach { triple =>
      if (!g2.contains(triple)) graph add triple
    }
    graph
  }

  // graph isomorphism
  def isomorphism(left: Rdf4j#Graph, right: Rdf4j#Graph): Boolean = {
    val leftNoContext = left.asScala.map(s => makeTriple(s.getSubject, s.getPredicate, s.getObject)).asJava
    val rightNoContext = right.asScala.map(s => makeTriple(s.getSubject, s.getPredicate, s.getObject)).asJava
    Models.isomorphic(leftNoContext, rightNoContext)
  }

  def graphSize(g: Rdf4j#Graph): Int = g.size()

}

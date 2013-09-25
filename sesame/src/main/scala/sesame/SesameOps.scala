package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.model.util._
import scala.collection.JavaConverters._

import SesamePrefix._

object SesameOperations extends RDFOps[Sesame] {

  val valueFactory: ValueFactory = ValueFactoryImpl.getInstance()

  // graph

  def emptyGraph: Sesame#Graph = new LinkedHashModel

  def makeGraph(it: Iterable[Sesame#Triple]): Sesame#Graph = {
    val graph = new LinkedHashModel
    it foreach { t => graph add t }
    graph
  }

  def graphToIterable(graph: Sesame#Graph): Iterable[Sesame#Triple] = graph.asScala

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


//    try {
//      valueFactory.createURI(iriStr).asInstanceOf[Sesame#URI]
//    } catch {
//      case e: Exception =>
//        if (iriStr.nonEmpty && iriStr.charAt(0) == '#')
//          new URI {
//            override def equals(o: Any): Boolean = o.isInstanceOf[URI] && o.asInstanceOf[URI].toString == this.toString
//            def getLocalName: String = iriStr
//            def getNamespace: String = ""
//            override def hashCode: Int = iriStr.hashCode
//            override def toString: String = iriStr
//            def stringValue: String = iriStr
//          }
//        else {
//          throw e
//        }
//    }

  def fromUri(node: Sesame#URI): String = node.toString

  // bnode

  def makeBNode() = valueFactory.createBNode()

  def makeBNodeLabel(label: String): Sesame#BNode = new BNodeImpl(label)

  def fromBNode(bn: Sesame#BNode): String = bn.getID

  // literal

  def foldLiteral[T](literal: Sesame#Literal)(funTL: Sesame#TypedLiteral => T, funLL: Sesame#LangLiteral => T): T =
    literal match {
      case typedLiteral: Sesame#TypedLiteral if literal.getLanguage == null || literal.getLanguage.isEmpty =>
        funTL(typedLiteral)
      case langLiteral: Sesame#LangLiteral => funLL(langLiteral)
    }

  // typed literal

  def makeTypedLiteral(lexicalForm: String, iri: Sesame#URI): Sesame#TypedLiteral = new LiteralImpl(lexicalForm, iri)

  def fromTypedLiteral(typedLiteral: Sesame#TypedLiteral): (String, Sesame#URI) = {
    val lexicalForm = typedLiteral.getLabel
    val typ = typedLiteral.getDatatype
    if (typedLiteral.getLanguage == null) {
      if (typ != null)
        (lexicalForm, typ)
      else
        (lexicalForm, makeUri("http://www.w3.org/2001/XMLSchema#string"))
    } else {
      throw new RuntimeException("fromTypedLiteral: " + typedLiteral.toString() + " must be a TypedLiteral")
    }
  }

  // lang literal

  def makeLangLiteral(lexicalForm: String, lang: Sesame#Lang): Sesame#LangLiteral = {
    val langString = fromLang(lang)
    new LiteralImpl(lexicalForm, langString)
  }

  def fromLangLiteral(langLiteral: Sesame#LangLiteral): (String, Sesame#Lang) = {
    val l = langLiteral.getLanguage
    if (l != null && l != "")
      (langLiteral.getLabel, makeLang(l))
    else
      throw new RuntimeException("fromLangLiteral: " + langLiteral.toString() + " must be a LangLiteral")
  }

  // lang

  def makeLang(langString: String) = langString

  def fromLang(lang: Sesame#Lang) = lang

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
        graphs.foreach(g => graphToIterable(g) foreach { t => graph add t })
        graph
    }
  }

  // graph isomorphism

  /** the new ModelUtil.equals changed its semantics. See 
    * - https://openrdf.atlassian.net/browse/SES-1695
    * - https://groups.google.com/forum/#!topic/sesame-devel/CGFDn7mESLg/discussion
    */
  def isomorphism(left: Sesame#Graph, right: Sesame#Graph): Boolean = {
    val leftNoContext = left.asScala.map(s => makeTriple(s.getSubject, s.getPredicate, s.getObject)).asJava
    val rightNoContext = right.asScala.map(s => makeTriple(s.getSubject, s.getPredicate, s.getObject)).asJava
    ModelUtil.equals(leftNoContext, rightNoContext)
  }
}

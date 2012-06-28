package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.model.util._
import scala.collection.JavaConverters._

import SesamePrefix._

object SesameOperations extends RDFOperations[Sesame] {

  // graph

  def emptyGraph: Sesame#Graph = new GraphImpl

  def makeGraph(it: Iterable[Sesame#Triple]): Sesame#Graph = {
    val graph = new GraphImpl
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

  def makeUri(iriStr: String): Sesame#URI = ValueFactoryImpl.getInstance.createURI(iriStr).asInstanceOf[Sesame#URI]

  def fromUri(node: Sesame#URI): String = node.toString

  // bnode

  def makeBNode() = ValueFactoryImpl.getInstance().createBNode()

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

  def getObjects(graph: Sesame#Graph, subject: Sesame#Node, predicate: Sesame#URI): Iterable[Sesame#Node] = {

    def iterable(subject: org.openrdf.model.Resource) = new Iterable[Sesame#Node] {
      def iterator = GraphUtil.getObjectIterator(graph, subject, predicate).asScala
    }

    foldNode(subject)(
      iri => iterable(iri),
      bnode => iterable(bnode),
      lit => Seq.empty
    )
  }

  def getPredicates(graph: Sesame#Graph, subject: Sesame#Node): Iterable[Sesame#URI] = {

    def iterable(subject: org.openrdf.model.Resource) = new Iterable[Sesame#URI] {
      def iterator = graph.`match`(subject, null, null).asScala map { statement => statement.getPredicate() }
    }

    foldNode(subject)(
      iri => iterable(iri),
      bnode => iterable(bnode),
      lit => Seq.empty
    )
  }

  def getSubjects(graph: Sesame#Graph, predicate: Sesame#URI, obj: Sesame#Node): Iterable[Sesame#Node] =
    new Iterable[Sesame#Node] {
      def iterator = GraphUtil.getSubjectIterator(graph, predicate, obj).asScala
    }

  // graph union

  def union(left: Sesame#Graph, right: Sesame#Graph): Sesame#Graph = {
    val graph = new GraphImpl
    graphToIterable(left) foreach { t => graph add t }
    graphToIterable(right) foreach { t => graph add t }
    graph
  }

  // graph isomorphism

  def isomorphism(left: Sesame#Graph, right: Sesame#Graph): Boolean =
    ModelUtil.equals(left, right)

}

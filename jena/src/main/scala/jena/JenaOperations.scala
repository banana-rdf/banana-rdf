package org.w3.banana.jena

import org.w3.banana._
import JenaUtil._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _ }
import com.hp.hpl.jena.rdf.model.AnonId
import com.hp.hpl.jena.datatypes.TypeMapper
import scala.collection.JavaConverters._
import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
import com.hp.hpl.jena.rdf.model.ResourceFactory._
import com.hp.hpl.jena.util.iterator._

object JenaOperations extends RDFOperations[Jena] {

  // graph

  def emptyGraph: Jena#Graph = Factory.createDefaultGraph

  def makeGraph(it: Iterable[Jena#Triple]): Jena#Graph = {
    val graph = emptyGraph
    it foreach { t => graph add t }
    graph
  }

  def graphToIterable(graph: Jena#Graph): Iterable[Jena#Triple] = new Iterable[Jena#Triple] {
    val iterator = graph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY).asScala
  }

  // triple

  def makeTriple(s: Jena#Node, p: Jena#URI, o: Jena#Node): Jena#Triple = {
    JenaTriple.create(s, p, o)
  }

  def fromTriple(t: Jena#Triple): (Jena#Node, Jena#URI, Jena#Node) = {
    val s = t.getSubject
    val p = t.getPredicate
    val o = t.getObject
    if (p.isInstanceOf[Jena#URI])
      (s, p.asInstanceOf[Jena#URI], o)
    else
      throw new RuntimeException("fromTriple: predicate " + p.toString + " must be a URI")
  }

  // node

  def foldNode[T](node: Jena#Node)(funURI: Jena#URI => T, funBNode: Jena#BNode => T, funLiteral: Jena#Literal => T): T = node match {
    case iri: Jena#URI => funURI(iri)
    case bnode: Jena#BNode => funBNode(bnode)
    case literal: Jena#Literal => funLiteral(literal)
  }

  // URI

  def makeUri(iriStr: String): Jena#URI = { JenaNode.createURI(iriStr).asInstanceOf[Node_URI] }

  def fromUri(node: Jena#URI): String =
    if (node.isURI)
      node.getURI
    else
      throw new RuntimeException("fromUri: " + node.toString() + " must be a URI")

  // bnode

  def makeBNode() = JenaNode.createAnon().asInstanceOf[Node_Blank]

  def makeBNodeLabel(label: String): Jena#BNode = {
    val id = AnonId.create(label)
    JenaNode.createAnon(id).asInstanceOf[Node_Blank]
  }

  def fromBNode(bn: Jena#BNode): String =
    if (bn.isBlank)
      bn.getBlankNodeId.getLabelString
    else
      throw new RuntimeException("fromBNode: " + bn.toString + " must be a BNode")

  // literal

  lazy val mapper = TypeMapper.getInstance

  def jenaDatatype(datatype: Jena#URI) = {
    val iriString = fromUri(datatype)
    mapper.getTypeByName(iriString)
  }

  /**
   * LangLiteral are not different types in Jena
   * we can discriminate on the lang tag presence
   */
  def foldLiteral[T](literal: Jena#Literal)(funTL: Jena#TypedLiteral => T, funLL: Jena#LangLiteral => T): T = literal match {
    case typedLiteral: Jena#TypedLiteral if literal.getLiteralLanguage == null || literal.getLiteralLanguage.isEmpty =>
      funTL(typedLiteral)
    case langLiteral: Jena#LangLiteral => funLL(langLiteral)
  }

  // typed literal

  def makeTypedLiteral(lexicalForm: String, iri: Jena#URI): Jena#TypedLiteral = {
    JenaNode.createLiteral(lexicalForm, null, jenaDatatype(iri)).asInstanceOf[Node_Literal]
  }

  def fromTypedLiteral(typedLiteral: Jena#TypedLiteral): (String, Jena#URI) = {
    val typ = typedLiteral.getLiteralDatatype
    if (typ != null)
      (typedLiteral.getLiteralLexicalForm.toString, makeUri(typ.getURI))
    else if (typedLiteral.getLiteralLanguage.isEmpty)
      (typedLiteral.getLiteralLexicalForm.toString, makeUri("http://www.w3.org/2001/XMLSchema#string"))
    else
      throw new RuntimeException("fromTypedLiteral: " + typedLiteral.toString() + " must be a TypedLiteral")
  }

  // lang literal

  def makeLangLiteral(lexicalForm: String, lang: Jena#Lang): Jena#LangLiteral = {
    val langString = fromLang(lang)
    JenaNode.createLiteral(lexicalForm, langString, null).asInstanceOf[Node_Literal]
  }

  def fromLangLiteral(langLiteral: Jena#LangLiteral): (String, Jena#Lang) = {
    val l = langLiteral.getLiteralLanguage
    if (l != "")
      (langLiteral.getLiteralLexicalForm.toString, makeLang(l))
    else
      throw new RuntimeException("fromLangLiteral: " + langLiteral.toString() + " must be a LangLiteral")
  }

  // lang

  def makeLang(langString: String) = langString

  def fromLang(lang: Jena#Lang) = lang

  // graph traversal

  def getObjects(graph: Jena#Graph, subject: Jena#Node, predicate: Jena#URI): Iterable[Jena#Node] = {
    val model = ModelFactory.createModelForGraph(graph)
    val subjectResource = foldNode(subject)(
      uri => model.createResource(fromUri(uri)),
      bnode => {
        val label = fromBNode(bnode)
        model.createResource(new AnonId(label))
      },
      lit => throw new RuntimeException("shouldn't use a literal here")
    )
    val p = fromUri(predicate)
    val it: Iterator[Jena#Node] = model.listObjectsOfProperty(subjectResource, createProperty(p)).asScala map toNode
    new Iterable[Jena#Node] {
      def iterator = it
    }
  }

  def getPredicates(graph: Jena#Graph, subject: Jena#Node): Iterable[Jena#URI] = {
    val predicates: Iterator[Jena#URI] = graph.find(subject, JenaNode.ANY, JenaNode.ANY).asScala map { triple => triple.getPredicate().asInstanceOf[Node_URI] }
    new Iterable[Jena#URI] {
      def iterator = predicates
    }
  }

  def getSubjects(graph: Jena#Graph, predicate: Jena#URI, obj: Jena#Node): Iterable[Jena#Node] = {
    val subjects: Iterator[Jena#Node] = graph.find(JenaNode.ANY, JenaNode.ANY, obj).asScala map { triple => fromTriple(triple)._1 }
    new Iterable[Jena#Node] {
      def iterator = subjects
    }
  }

  // graph union

  def union(left: Jena#Graph, right: Jena#Graph): Jena#Graph = {
    if (left.isEmpty)
      right
    else if (right.isEmpty)
      left
    else {
      val graph = Factory.createDefaultGraph
      graphToIterable(left) foreach { t => graph add t }
      graphToIterable(right) foreach { t => graph add t }
      graph
    }
  }

  // graph isomorphism

  def isomorphism(left: Jena#Graph, right: Jena#Graph): Boolean =
    left isIsomorphicWith right

}

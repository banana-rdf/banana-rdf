package org.w3.banana.huron

import org.w3.banana._
import play.api.libs.json._

object HuronOps extends RDFOps[Huron] {

  /*
   node:
   uri ["u", "http://..."]
   bnode ["b", "1234"]
   typed literal ["t", "alexandre", "xsd:string"]
   lang literal ["l", "alex", "fr"]
   triple:
   [s, p, o]
   graph:
   [n1, n2, n3, ...]
   */

  // graph

  def emptyGraph: Huron#Graph = Set.empty

  def makeGraph(it: Iterable[Huron#Triple]): Huron#Graph = {
    it.toSet
  }

  def graphToIterable(graph: Huron#Graph): Iterable[Huron#Triple] = graph

  // triple

  def makeTriple(s: Huron#Node, p: Huron#URI, o: Huron#Node): Huron#Triple =
    JsArray(Array(s, p, o))

  def fromTriple(t: Huron#Triple): (Huron#Node, Huron#URI, Huron#Node) = {
    val Seq(s, p, o) = t.value
    (s.asInstanceOf[Huron#Node], p.asInstanceOf[Huron#Node], o.asInstanceOf[Huron#Node])
  }

  // node

  // [node type as String, ]

  def foldNode[T](node: Huron#Node)(funURI: Huron#URI => T, funBNode: Huron#BNode => T, funLiteral: Huron#Literal => T): T = {
    val typ = node.value.head.asInstanceOf[JsString].value
    typ match {
      case "u" => funURI(node)
      case "b" => funBNode(node)
      case _ => funLiteral(node)
    }
  }

  // URI

  def makeUri(uriStr: String): Huron#URI = {
    Json.arr("u", uriStr)
  }

  def fromUri(uri: Huron#URI): String = {
    uri.value(1).asInstanceOf[JsString].value
  }

  // bnode

  def makeBNode(): Huron#BNode = {
    val label = java.util.UUID.randomUUID().toString
    makeBNodeLabel(label)
  }

  def makeBNodeLabel(label: String): Huron#BNode = {
    Json.arr("b", label)
  }

  def fromBNode(bn: Huron#BNode): String = {
    bn.value(1).asInstanceOf[JsString].value
  }

  // literal

  def foldLiteral[T](literal: Huron#Literal)(funTL: Huron#TypedLiteral => T, funLL: Huron#LangLiteral => T): T = {
    val typ = literal.value.head.asInstanceOf[JsString].value
    typ match {
      case "t" => funTL(literal)
      case "l" => funLL(literal)
    }
  }

  // typed literal

  def makeTypedLiteral(lexicalForm: String, uri: Huron#URI): Huron#TypedLiteral = {
    val uriStr = fromUri(uri)
    Json.arr("t", lexicalForm, uriStr)
  }

  def fromTypedLiteral(typedLiteral: Huron#TypedLiteral): (String, Huron#URI) = {
    val seq = typedLiteral.value
    val lexicalForm = seq(1).asInstanceOf[JsString].value
    val uriStr = seq(2).asInstanceOf[JsString].value
    val typeUri = makeUri(uriStr)
    (lexicalForm, typeUri)
  }

  // lang literal

  def makeLangLiteral(lexicalForm: String, lang: Huron#Lang): Huron#LangLiteral = {
    Json.arr("l", lexicalForm, lang)
  }

  def fromLangLiteral(langLiteral: Huron#LangLiteral): (String, Huron#Lang) = {
    val seq = langLiteral.value
    val lexicalForm = seq(1).asInstanceOf[JsString].value
    val lang = seq(2).asInstanceOf[JsString].value
    (lexicalForm, lang)
  }

  // lang

  def makeLang(langString: String): Huron#Lang = langString

  def fromLang(lang: Huron#Lang): String = lang

  // graph traversal

  val ANY: Huron#NodeAny = model.ANY

  implicit def toConcreteNodeMatch(node: Huron#Node): Huron#NodeMatch = model.PlainNode(node)

  def foldNodeMatch[T](nodeMatch: Huron#NodeMatch)(funANY: => T, funConcrete: Huron#Node => T): T =
    nodeMatch match {
      case model.ANY => funANY
      case model.PlainNode(node) => funConcrete(node)
    }

  def find(graph: Huron#Graph, subject: Huron#NodeMatch, predicate: Huron#NodeMatch, objectt: Huron#NodeMatch): Iterator[Huron#Triple] = {
    graph.iterator filter { triple =>
      val seq = triple.value
      def matchSubject = subject == ANY || subject == model.PlainNode(seq(0).asInstanceOf[Huron#Node])
      def matchPredicate = predicate == ANY || predicate == model.PlainNode(seq(1).asInstanceOf[Huron#Node])
      def matchObject = objectt == ANY || objectt == model.PlainNode(seq(2).asInstanceOf[Huron#Node])
      matchSubject && matchPredicate && matchObject
    }
  }

  // graph union

  def union(graphs: List[Huron#Graph]): Huron#Graph = {
    var g = Set.empty[Huron#Triple]
    graphs foreach { graph => g ++= graph }
    g
  }

  // graph isomorphism

  import org.w3.banana.plantain._
  val transformer = new RDFTransformer[Huron, Plantain](HuronOps, PlantainOps)

  def isomorphism(left: Huron#Graph, right: Huron#Graph): Boolean = {
    val l = transformer.transform(left)
    val r = transformer.transform(right)
    PlantainOps.isomorphism(l, r)
  }

}

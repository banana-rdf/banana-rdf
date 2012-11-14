package org.w3.banana.plantain

import org.w3.banana._

object PlantainOps extends RDFOps[Plantain] {

  // graph

  val emptyGraph: Plantain#Graph = plantain.Graph.empty

  def makeGraph(triples: Iterable[Plantain#Triple]): Plantain#Graph =
    triples.foldLeft(emptyGraph){ _ + _ }

  def graphToIterable(graph: Plantain#Graph): Iterable[Plantain#Triple] = graph.triples

  // triple

  def makeTriple(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): Plantain#Triple =
    plantain.Triple(s, p, o)

  def fromTriple(t: Plantain#Triple): (Plantain#Node, Plantain#URI, Plantain#Node) =
    (t.subject, t.predicate, t.objectt)

  // node

  def foldNode[T](node: Plantain#Node)(funURI: Plantain#URI => T, funBNode: Plantain#BNode => T, funLiteral: Plantain#Literal => T): T = node match {
    case uri@plantain.URI(_) => funURI(uri)
    case bnode@plantain.BNode(_) => funBNode(bnode)
    case literal: plantain.Literal => funLiteral(literal)
  }

  // URI

  def makeUri(uriStr: String): Plantain#URI = plantain.URI.fromString(uriStr)

  def fromUri(node: Plantain#URI): String = node.underlying.toString

  // bnode

  def makeBNode(): Plantain#BNode = plantain.BNode(java.util.UUID.randomUUID().toString)

  def makeBNodeLabel(label: String): Plantain#BNode = plantain.BNode(label)

  def fromBNode(bn: Plantain#BNode): String = bn.label

  // literal

  def foldLiteral[T](literal: Plantain#Literal)(funTL: Plantain#TypedLiteral => T, funLL: Plantain#LangLiteral => T): T = literal match {
    case tl@plantain.TypedLiteral(_, _) => funTL(tl)
    case ll@plantain.LangLiteral(_, _) => funLL(ll)
  }

  // typed literal

  def makeTypedLiteral(lexicalForm: String, uri: Plantain#URI): Plantain#TypedLiteral =
    plantain.TypedLiteral(lexicalForm, uri)

  def fromTypedLiteral(typedLiteral: Plantain#TypedLiteral): (String, Plantain#URI) =
    (typedLiteral.lexicalForm, typedLiteral.uri)

  // lang literal

  def makeLangLiteral(lexicalForm: String, lang: Plantain#Lang): Plantain#LangLiteral =
    plantain.LangLiteral(lexicalForm, lang)

  def fromLangLiteral(langLiteral: Plantain#LangLiteral): (String, Plantain#Lang) =
    (langLiteral.lexicalForm, langLiteral.lang)

  // lang

  def makeLang(langString: String): Plantain#Lang = langString

  def fromLang(lang: Plantain#Lang): String = lang

  // graph traversal

  val ANY: Plantain#NodeAny = plantain.ANY

  implicit def toConcreteNodeMatch(node: Plantain#Node): Plantain#NodeMatch = PlainNode(node)

  def foldNodeMatch[T](nodeMatch: Plantain#NodeMatch)(funANY: => T, funConcrete: Plantain#Node => T): T =
    nodeMatch match {
      case plantain.ANY => funANY
      case PlainNode(node) => funConcrete(node)
    }

  def find(graph: Plantain#Graph, subject: Plantain#NodeMatch, predicate: Plantain#NodeMatch, objectt: Plantain#NodeMatch): Iterator[Plantain#Triple] =
    graph.find(subject, predicate, objectt).iterator

  // graph union

  def union(graphs: List[Plantain#Graph]): Plantain#Graph =
    graphs.foldLeft(Graph.empty){ _ union _ }

  // graph isomorphism

  def isomorphism(left: Plantain#Graph, right: Plantain#Graph): Boolean =
    left isIsomorphicWith right

}

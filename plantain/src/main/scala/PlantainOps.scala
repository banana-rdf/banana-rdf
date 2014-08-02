package org.w3.banana.plantain

import java.util.NoSuchElementException

import akka.http.model.Uri
import org.w3.banana._

object PlantainOps extends RDFOps[Plantain] with PlantainURIOps {

  // graph

  val emptyGraph: Plantain#Graph = model.Graph(Map.empty, 0)

  def makeGraph(triples: Iterable[Plantain#Triple]): Plantain#Graph =
    triples.foldLeft(emptyGraph) { _ + _ }

  def graphToIterable(graph: Plantain#Graph): Iterable[Plantain#Triple] = graph.triples

  // triple

  def makeTriple(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): Plantain#Triple =
    model.Triple(s, p, o)

  def fromTriple(t: Plantain#Triple): (Plantain#Node, Plantain#URI, Plantain#Node) =
    (t.subject, t.predicate, t.objectt)

  // node

  def foldNode[T](node: Plantain#Node)(funURI: Plantain#URI => T, funBNode: Plantain#BNode => T, funLiteral: Plantain#Literal => T): T = node match {
    case uri @ URI(_) => funURI(uri)
    case bnode @ BNode(_) => funBNode(bnode)
    case literal @ Literal(_, _, _) => funLiteral(literal)
  }

  // URI

  def makeUri(uriStr: String): Plantain#URI = model.URI(Uri(uriStr))

  def fromUri(node: Plantain#URI): String = node.underlying.toString

  // bnode

  def makeBNode(): Plantain#BNode = model.BNode(java.util.UUID.randomUUID().toString)

  def makeBNodeLabel(label: String): Plantain#BNode = model.BNode(label)

  def fromBNode(bn: Plantain#BNode): String = bn.label

  // literal

  val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  def makeLiteral(lexicalForm: String, datatype: Plantain#URI): Plantain#Literal =
    model.Literal(lexicalForm, datatype, None)

  def makeLangTaggedLiteral(lexicalForm: String, lang: Plantain#Lang): Plantain#Literal =
    model.Literal(lexicalForm, __rdfLangString, Some(lang))

  def fromLiteral(literal: Plantain#Literal): (String, Plantain#URI, Option[Plantain#Lang]) =
    (literal.lexicalForm, literal.datatype, literal.langOpt)

  // lang

  def makeLang(langString: String): Plantain#Lang = langString

  def fromLang(lang: Plantain#Lang): String = lang

  // graph traversal

  val ANY: Plantain#NodeAny = model.ANY

  implicit def toConcreteNodeMatch(node: Plantain#Node): Plantain#NodeMatch = model.PlainNode(node)

  def foldNodeMatch[T](nodeMatch: Plantain#NodeMatch)(funANY: => T, funConcrete: Plantain#Node => T): T =
    nodeMatch match {
      case model.ANY => funANY
      case model.PlainNode(node) => funConcrete(node)
    }

  def find(graph: Plantain#Graph, subject: Plantain#NodeMatch, predicate: Plantain#NodeMatch, objectt: Plantain#NodeMatch): Iterator[Plantain#Triple] =
    graph.find(subject, predicate, objectt).iterator

  // graph union

  def union(graphs: Seq[Plantain#Graph]): Plantain#Graph =
    graphs.foldLeft(Graph.empty) { _ union _ }

  def diff(g1: Plantain#Graph, g2: Plantain#Graph): Plantain#Graph = {
    @annotation.tailrec
    def loop(g: Plantain#Graph, triples: Iterator[Plantain#Triple]): Plantain#Graph = {
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

 def isomorphism(left: Plantain#Graph, right: Plantain#Graph): Boolean =
   GraphEquivalence.findAnswer(left,right).isSuccess

}

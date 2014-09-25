package org.w3.banana.plantain.generic

import java.util.NoSuchElementException
import org.w3.banana.plantain.model

import org.w3.banana._
import org.w3.banana.iso.{ GraphIsomorphism, SimpleMappingGenerator, VerticeCBuilder }
import scala.language.existentials

trait PlantainOps[U, Rdf <: Plantain[U]] extends RDFOps[Rdf] {
  implicit val ops: RDFOps[Rdf] = this

  // graph

  val emptyGraph: Rdf#Graph = model.Graph[U](Map.empty, 0)

  def makeGraph(triples: Iterable[Rdf#Triple]): Rdf#Graph =
    triples.foldLeft(emptyGraph) { (g, triple) => g + triple }

  def getTriples(graph: Rdf#Graph): Iterable[Rdf#Triple] = graph.triples

  // triple

  def makeTriple(s: Rdf#Node, p: Rdf#URI, o: Rdf#Node): Rdf#Triple =
    model.Triple[U](s, p, o)

  def fromTriple(t: Rdf#Triple): (Rdf#Node, Rdf#URI, Rdf#Node) =
    (t.subject, t.predicate, t.objectt)

  // node

  def foldNode[T](node: Rdf#Node)(funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T = node match {
    case uri: Rdf#URI => funURI(uri)
    case bnode: Rdf#BNode => funBNode(bnode)
    case literal: Rdf#Literal => funLiteral(literal)
  }

  // URI

  def fromUri(node: Rdf#URI): String = node.underlying.toString

  // bnode

  def makeBNode(): Rdf#BNode

  def makeBNodeLabel(label: String): Rdf#BNode = model.BNode(label)

  def fromBNode(bn: Rdf#BNode): String = bn.label

  // literal

  val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  def makeLiteral(lexicalForm: String, datatype: Rdf#URI): Rdf#Literal =
    model.Literal(lexicalForm, datatype, None)

  def makeLangTaggedLiteral(lexicalForm: String, lang: Rdf#Lang): Rdf#Literal =
    model.Literal(lexicalForm, __rdfLangString, Some(lang))

  def fromLiteral(literal: Rdf#Literal): (String, Rdf#URI, Option[Rdf#Lang]) =
    (literal.lexicalForm, literal.datatype, literal.langOpt)

  // lang

  def makeLang(langString: String): Rdf#Lang = langString

  def fromLang(lang: Rdf#Lang): String = lang

  // graph traversal

  val ANY: Rdf#NodeAny = model.ANY

  implicit def toConcreteNodeMatch(node: Rdf#Node): Rdf#NodeMatch = model.PlainNode(node)

  def foldNodeMatch[T](nodeMatch: Rdf#NodeMatch)(funANY: => T, funConcrete: Rdf#Node => T): T =
    nodeMatch match {
      case model.ANY => funANY
      case model.PlainNode(node) => funConcrete(node)
    }

  def find(graph: Rdf#Graph, subject: Rdf#NodeMatch, predicate: Rdf#NodeMatch, objectt: Rdf#NodeMatch): Iterator[Rdf#Triple] =
    graph.find(subject, predicate, objectt).iterator

  // graph union

  def union(graphs: Seq[Rdf#Graph]): Rdf#Graph =
    graphs.foldLeft(Graph.empty) { (g1, g2) => g1.union(g2) }

  def diff(g1: Rdf#Graph, g2: Rdf#Graph): Rdf#Graph = {
    @annotation.tailrec
    def loop(g: Rdf#Graph, triples: Iterator[Rdf#Triple]): Rdf#Graph = {
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
  private lazy val iso = new GraphIsomorphism[Rdf](new SimpleMappingGenerator[Rdf](VerticeCBuilder.simpleHash))

  def isomorphism(left: Rdf#Graph, right: Rdf#Graph): Boolean = {
    iso.findAnswer(left, right).isSuccess
  }

  def graphSize(g: Rdf#Graph): Int = g.size

}

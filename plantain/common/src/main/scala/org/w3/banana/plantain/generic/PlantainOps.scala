package org.w3.banana.plantain.generic

import java.util.NoSuchElementException
import org.w3.banana.plantain.model

import org.w3.banana._
import org.w3.banana.isomorphism.{ GraphIsomorphism, SimpleMappingGenerator, VerticeCBuilder }
import scala.language.existentials

/*
abstract class PlantainOps[U] extends RDFOps[Plantain[U]] {

  type P = Plantain[U]

  // graph

  val emptyGraph: Plantain[U]#Graph = model.Graph(Map.empty, 0)

  def makeGraph(triples: Iterable[Plantain[U]#Triple]): Plantain[U]#Graph =
    triples.foldLeft(emptyGraph) { (g, triple) => g + triple }

  def getTriples(graph: Plantain[U]#Graph): Iterable[Plantain[U]#Triple] = graph.triples

  // triple

  def makeTriple(s: Plantain[U]#Node, p: Plantain[U]#URI, o: Plantain[U]#Node): Plantain[U]#Triple =
    model.Triple[U](s, p, o)

  def fromTriple(t: P#Triple): (P#Node, P#URI, P#Node) =
    (t.subject, t.predicate, t.objectt)

  // node

  def foldNode[T](node: P#Node)(funURI: P#URI => T, funBNode: P#BNode => T, funLiteral: P#Literal => T): T = node match {
    case uri @ model.URI(_)               => funURI(uri)
    case bnode @ model.BNode(_)           => funBNode(bnode)
    case literal @ model.Literal(_, _, _) => funLiteral(literal)
  }

  // URI

  def fromUri(node: P#URI): String = node.underlying.toString

  // bnode

  def makeBNode(): P#BNode

  def makeBNodeLabel(label: String): P#BNode = model.BNode(label)

  def fromBNode(bn: P#BNode): String = bn.label

  // literal

  val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  def makeLiteral(lexicalForm: String, datatype: P#URI): P#Literal =
    model.Literal(lexicalForm, datatype, None)

  def makeLangTaggedLiteral(lexicalForm: String, lang: P#Lang): P#Literal =
    model.Literal(lexicalForm, __rdfLangString, Some(lang))

  def fromLiteral(literal: P#Literal): (String, P#URI, Option[P#Lang]) =
    (literal.lexicalForm, literal.datatype, literal.langOpt)

  // lang

  def makeLang(langString: String): P#Lang = langString

  def fromLang(lang: P#Lang): String = lang

  // graph traversal

  val ANY: P#NodeAny = model.ANY

  implicit def toConcreteNodeMatch(node: P#Node): P#NodeMatch = model.PlainNode(node)

  def foldNodeMatch[T](nodeMatch: P#NodeMatch)(funANY: => T, funConcrete: P#Node => T): T =
    nodeMatch match {
      case model.ANY             => funANY
      case model.PlainNode(node) => funConcrete(node)
    }

  def find(graph: P#Graph, subject: P#NodeMatch, predicate: P#NodeMatch, objectt: P#NodeMatch): Iterator[P#Triple] =
    graph.find(subject, predicate, objectt).iterator

  // graph union

  def union(graphs: Seq[P#Graph]): P#Graph =
    graphs.foldLeft(Graph.empty) { (g1, g2) => g1.union(g2) }

  def diff(g1: P#Graph, g2: P#Graph): P#Graph = {
    @annotation.tailrec
    def loop(g: P#Graph, triples: Iterator[P#Triple]): P#Graph = {
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
  val iso = new GraphIsomorphism[P](new SimpleMappingGenerator[P](VerticeCBuilder.simpleHash(this))(this))(this)

  def isomorphism(left: P#Graph, right: P#Graph): Boolean = {
    iso.findAnswer(left, right).isSuccess
  }

  def graphSize(g: P#Graph): Int = g.size

}

 */

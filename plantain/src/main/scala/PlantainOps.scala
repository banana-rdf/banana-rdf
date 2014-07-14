package org.w3.banana.plantain

import org.w3.banana._
import model._
import akka.http.model.Uri

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
        loop(g.removeExistingTriple(triple), triples)
      } else {
        g
      }
    }
    loop(g1, g2.triples.iterator)
  }

  // graph isomorphism

  // TODO: remove dependency on Sesame
  // the definition for RDF Graph isomorphism can be found at http://www.w3.org/TR/2014/REC-rdf11-concepts-20140225/#h3_graph-isomorphism
  // here is an old paper discussing implementation details http://www.hpl.hp.com/techreports/2001/HPL-2001-293.pdf
  def isomorphism(left: Plantain#Graph, right: Plantain#Graph): Boolean = {
    // as long as Sesame is in scope, let's just rely on it for the complex stuff
    import org.openrdf.{ model => sesame }
    import org.openrdf.model.impl._
    import org.openrdf.model.util.ModelUtil
    def statement(s: model.Node, p: model.URI, o: model.Node): sesame.Statement = {
      val subject: sesame.Resource = s match {
        case model.URI(uri) => new URIImpl(uri.toString)
        case model.BNode(label) => new BNodeImpl(label)
        case literal @ model.Literal(_, _, _) => throw new IllegalArgumentException(s"$literal was in subject position")
      }
      val predicate: sesame.URI = p match {
        case model.URI(uri) => new URIImpl(uri.toString)
      }
      val objectt: sesame.Value = o match {
        case model.URI(uri) => new URIImpl(uri.toString)
        case model.BNode(label) => new BNodeImpl(label)
        case model.Literal(lexicalForm, model.URI(uri), None) => new LiteralImpl(lexicalForm, new URIImpl(uri.toString))
        case model.Literal(lexicalForm, _, Some(lang)) => new LiteralImpl(lexicalForm, lang)
      }
      new StatementImpl(subject, predicate, objectt)
    }
    def graph(g: Plantain#Graph): sesame.Graph = {
      val graph = new LinkedHashModel
      @annotation.tailrec
      def loop(triples: Iterator[Plantain#Triple]): Unit = {
        if (triples.hasNext) {
          val Triple(s, p, o) = triples.next()
          graph.add(statement(s, p, o))
          loop(triples)
        } else {
          ()
        }
      }
      loop(g.triples.iterator)
      graph
    }
    ModelUtil.equals(graph(left), graph(right))
  }

}

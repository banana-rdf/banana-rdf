package org.w3.banana
package n3js

import java.util.UUID
import org.w3.banana.isomorphism._

object N3jsOps extends RDFOps[N3js] with N3jsMGraphOps with DefaultURIOps[N3js] {

  import N3.Util

  // graph

  final val emptyGraph: N3js#Graph = plantain.model.Graph(Map.empty, 0)

  final def makeGraph(triples: Iterable[N3js#Triple]): N3js#Graph =
    triples.foldLeft(emptyGraph) { case (g, (s, p, o)) => g + (s, p, o) }

  final def getTriples(graph: N3js#Graph): Iterable[N3js#Triple] = graph.triples

  def graphSize(graph: N3js#Graph): Int = graph.size

  // triple

  final def makeTriple(s: N3js#Node, p: N3js#URI, o: N3js#Node): N3js#Triple =
    (s, p, o)

  final def fromTriple(t: N3js#Triple): (N3js#Node, N3js#URI, N3js#Node) = t

  // node

  final def foldNode[T](
    node: N3js#Node)(
    funURI: N3js#URI => T,
    funBNode: N3js#BNode => T,
    funLiteral: N3js#Literal => T
  ): T = node match {
    case bnode @ BNode(_)               => funBNode(bnode)
    case s: String if Util.isIRI(s)     => funURI(s)
    case s: String if Util.isLiteral(s) => funLiteral(s)
  }

  // URI

  final def fromUri(uri: N3js#URI): String = uri

  final def makeUri(s: String): N3js#URI = s

  // bnode

  final def makeBNode(): N3js#BNode = model.BNode(UUID.randomUUID().toString)

  final def makeBNodeLabel(label: String): N3js#BNode = model.BNode(label)

  final def fromBNode(bnode: N3js#BNode): String = bnode.label

  // literal

  final val __rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

  final def makeLiteral(lexicalForm: String, datatype: N3js#URI): N3js#Literal =
    Util.createLiteral(lexicalForm, datatype)

  final def makeLangTaggedLiteral(lexicalForm: String, lang: N3js#Lang): N3js#Literal =
    Util.createLiteral(lexicalForm, lang)

  final def fromLiteral(literal: N3js#Literal): (String, N3js#URI, Option[N3js#Lang]) = {
    val lang = Util.getLiteralLanguage(literal)
    val langOpt = if (lang.isEmpty) None else Some(lang)
    (Util.getLiteralValue(literal), Util.getLiteralType(literal), langOpt)
  }

  // lang

  final def makeLang(langString: String): N3js#Lang = langString

  final def fromLang(lang: N3js#Lang): String = lang

  // graph traversal

  final val ANY: N3js#NodeAny = null

  implicit def toConcreteNodeMatch(node: N3js#Node): N3js#NodeMatch = node

  final def foldNodeMatch[T](
    nodeMatch: N3js#NodeMatch)(
    funANY: => T,
      funConcrete: N3js#Node => T
  ): T = nodeMatch match {
    case null => funANY
    case node => funConcrete(node)
  }

  final def find(
    graph: N3js#Graph,
    subject: N3js#NodeMatch,
    predicate: N3js#NodeMatch,
    objectt: N3js#NodeMatch
  ): Iterator[N3js#Triple] = predicate match {
    case p: N3js#URI => graph.find(Option(subject), Some(p), Option(objectt)).iterator
    case null            => graph.find(Option(subject), None, Option(objectt)).iterator
    case p               => sys.error(s"[find] invalid value in predicate position: $p")
  }

  // graph union

  final def union(graphs: Seq[N3js#Graph]): N3js#Graph = {
    var mgraph = makeEmptyMGraph()
    graphs.foreach(graph => addTriples(mgraph, graph.triples))
    mgraph.graph
  }

  final def diff(g1: N3js#Graph, g2: N3js#Graph): N3js#Graph = {
    val mgraph = makeMGraph(g1)
    try { removeTriples(mgraph, g2.triples) } catch { case nsee: NoSuchElementException => () }
    mgraph.graph
  }

  // graph isomorphism

  final val iso = new GraphIsomorphism[N3js](
    new SimpleMappingGenerator[N3js](VerticeCBuilder.simpleHash(this))(this)
  )(this)

  final def isomorphism(left: N3js#Graph, right: N3js#Graph): Boolean = {
    iso.findAnswer(left, right).isSuccess
  }

}


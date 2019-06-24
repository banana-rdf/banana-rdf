package org.w3.banana.plantain

import java.math.BigInteger

import akka.http.scaladsl.model.Uri
import org.w3.banana._
import org.w3.banana.isomorphism._

object PlantainOps extends RDFOps[Plantain] with PlantainMGraphOps with PlantainURIOps {

  // graph

  final val emptyGraph: Plantain#Graph = model.IntHexastoreGraph.empty[Plantain#Node, Plantain#URI, Plantain#Node]

  final def makeGraph(triples: Iterable[Plantain#Triple]): Plantain#Graph =
    triples.foldLeft(emptyGraph) { case (g, (s, p, o)) => g + (s, p, o) }

  final def getTriples(graph: Plantain#Graph): Iterable[Plantain#Triple] = graph.triples

  def graphSize(graph: Plantain#Graph): Int = graph.size

  // triple

  final def makeTriple(s: Plantain#Node, p: Plantain#URI, o: Plantain#Node): Plantain#Triple =
    (s, p, o)

  final def fromTriple(t: Plantain#Triple): (Plantain#Node, Plantain#URI, Plantain#Node) = t

  // node

  final def foldNode[T](
    node: Plantain#Node)(
    funURI: Plantain#URI => T,
    funBNode: Plantain#BNode => T,
    funLiteral: Plantain#Literal => T
  ): T = node match {
    case uri: Plantain#URI  => funURI(uri)
    case bnode: model.BNode => funBNode(bnode)
    case literal: Any       => funLiteral(literal)
  }

  // URI

  final def fromUri(uri: Plantain#URI): String = uri.toString

  final def makeUri(s: String): Plantain#URI = Uri(s)

  // bnode

  final def makeBNode(): Plantain#BNode = model.BNode(java.util.UUID.randomUUID().toString)

  final def makeBNodeLabel(label: String): Plantain#BNode = model.BNode(label)

  final def fromBNode(bnode: Plantain#BNode): String = bnode.label

  // literal

  final val rdfLangString = makeUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
  final val xsdInteger = makeUri("http://www.w3.org/2001/XMLSchema#integer")
  final val xsdString = makeUri("http://www.w3.org/2001/XMLSchema#string")
  final val xsdBoolean = makeUri("http://www.w3.org/2001/XMLSchema#boolean")
  final val xsdDouble = makeUri("http://www.w3.org/2001/XMLSchema#double")

  final def makeLiteral(lexicalForm: String, datatype: Plantain#URI): Plantain#Literal = datatype match {
    case `xsdInteger`   => new BigInteger(lexicalForm)
    case `xsdString`    => lexicalForm
    case `xsdBoolean`   => lexicalForm.toBoolean
    case `xsdDouble`    => lexicalForm.toDouble
    case _              => model.Literal(lexicalForm, datatype, null)
  }

  final def makeLangTaggedLiteral(lexicalForm: String, lang: Plantain#Lang): Plantain#Literal =
    model.Literal(lexicalForm, rdfLangString, lang)

  final def fromLiteral(literal: Plantain#Literal): (String, Plantain#URI, Option[Plantain#Lang]) = literal match {
    case i: BigInteger => (i.toString, xsdInteger, None)
    case s: String     => (s, xsdString, None)
    case b: Boolean    => (b.toString, xsdBoolean, None)
    case d: Double     => (d.toString, xsdDouble, None)
    case model.Literal(lexicalForm, datatype, langOpt) =>
      (lexicalForm, datatype, Option(langOpt))
  }

  // lang

  final def makeLang(langString: String): Plantain#Lang = langString

  final def fromLang(lang: Plantain#Lang): String = lang

  // graph traversal

  final val ANY: Plantain#NodeAny = null

  implicit def toConcreteNodeMatch(node: Plantain#Node): Plantain#NodeMatch = node

  final def foldNodeMatch[T](
    nodeMatch: Plantain#NodeMatch)(
    funANY: => T,
      funConcrete: Plantain#Node => T
  ): T = nodeMatch match {
    case null => funANY
    case node => funConcrete(node)
  }

  final def find(
    graph: Plantain#Graph,
    subject: Plantain#NodeMatch,
    predicate: Plantain#NodeMatch,
    objectt: Plantain#NodeMatch
  ): Iterator[Plantain#Triple] = predicate match {
    case p: Plantain#URI => graph.find(Option(subject), Some(p), Option(objectt)).iterator
    case null            => graph.find(Option(subject), None, Option(objectt)).iterator
    case p               => sys.error(s"[find] invalid value in predicate position: $p")
  }

  // graph union

  final def union(graphs: Seq[Plantain#Graph]): Plantain#Graph = {
    var mgraph = makeEmptyMGraph()
    graphs.foreach(graph => addTriples(mgraph, graph.triples))
    mgraph.graph
  }

  final def diff(g1: Plantain#Graph, g2: Plantain#Graph): Plantain#Graph = {
    val mgraph = makeMGraph(g1)
    try { removeTriples(mgraph, g2.triples) } catch { case nsee: NoSuchElementException => () }
    mgraph.graph
  }

  // graph isomorphism

  final val iso = new GraphIsomorphism[Plantain](
    new SimpleMappingGenerator[Plantain](VerticeCBuilder.simpleHash(this))(this)
  )(this)

  final def isomorphism(left: Plantain#Graph, right: Plantain#Graph): Boolean = {
    iso.findAnswer(left, right).isSuccess
  }

}

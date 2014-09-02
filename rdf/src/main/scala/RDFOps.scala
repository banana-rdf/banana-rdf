package org.w3.banana

object RDFOps {

  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]): RDFOps[Rdf] = ops

}

/**
 * A Module that gathers the types needed to define an RDF implementation
 * Closely based on
 *   http://www.w3.org/TR/rdf11-concepts/
 * But with the  notable exceptions:
 *   - we allow literals in subject position
 */
trait RDFOps[Rdf <: RDF]
    extends URIOps[Rdf]
    with RDFDSL[Rdf]
    with CommonPrefixes[Rdf]
    with syntax.RDFSyntax[Rdf] {

  // graph

  def emptyGraph: Rdf#Graph

  def makeGraph(it: Iterable[Rdf#Triple]): Rdf#Graph

  def getTriples(graph: Rdf#Graph): Iterable[Rdf#Triple]

  // triple

  def makeTriple(s: Rdf#Node, p: Rdf#URI, o: Rdf#Node): Rdf#Triple

  def fromTriple(triple: Rdf#Triple): (Rdf#Node, Rdf#URI, Rdf#Node)

  // node

  def foldNode[T](node: Rdf#Node)(funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T

  def isURI[T](node: Rdf#Node) = foldNode[Boolean](node)(uri => true, bn => false, lit => false)

  // URI

  def makeUri(s: String): Rdf#URI

  def fromUri(uri: Rdf#URI): String

  // bnode

  def makeBNode(): Rdf#BNode

  def makeBNodeLabel(s: String): Rdf#BNode

  def fromBNode(bn: Rdf#BNode): String

  // literal

  def makeLiteral(lexicalForm: String, datatype: Rdf#URI): Rdf#Literal

  def makeLangTaggedLiteral(lexicalForm: String, lang: Rdf#Lang): Rdf#Literal

  def fromLiteral(literal: Rdf#Literal): (String, Rdf#URI, Option[Rdf#Lang])

  // lang

  def makeLang(s: String): Rdf#Lang

  def fromLang(l: Rdf#Lang): String

  // graph traversal

  def ANY: Rdf#NodeAny

  implicit def toConcreteNodeMatch(node: Rdf#Node): Rdf#NodeMatch

  def foldNodeMatch[T](nodeMatch: Rdf#NodeMatch)(funANY: => T, funNode: Rdf#Node => T): T

  def find(graph: Rdf#Graph, subject: Rdf#NodeMatch, predicate: Rdf#NodeMatch, objectt: Rdf#NodeMatch): Iterator[Rdf#Triple]

  // graph union
  def union(graphs: Seq[Rdf#Graph]): Rdf#Graph

  def diff(g1: Rdf#Graph, g2: Rdf#Graph): Rdf#Graph

  // graph isomorphism
  def isomorphism(left: Rdf#Graph, right: Rdf#Graph): Boolean

  // graph size
  def graphSize(g: Rdf#Graph): Int

  //  implicit def sparqlSolutionSyntax(solution: Rdf#Solution): syntax.SparqlSolutionSyntax[Rdf] = new syntax.SparqlSolutionSyntax[Rdf](solution)
  //
  //  implicit def sparqlSolutionsSyntax(solutions: Rdf#Solutions): syntax.SparqlSolutionsSyntax[Rdf] = new syntax.SparqlSolutionsSyntax[Rdf](solutions)

}

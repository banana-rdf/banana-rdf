package org.w3.banana

/**
 * A Module that gathers the types needed to define an RDF implementation
 * Closely based on
 *   http://dvcs.w3.org/hg/rdf/raw-file/default/rdf-concepts/index.html
 * But with the  notable exceptions:
 *   - we allow literals in subject position
 */
trait RDFOps[Rdf <: RDF]
    extends RDFDSL[Rdf]
    with CommonPrefixes[Rdf] {

  // graph

  def emptyGraph: Rdf#Graph

  def makeGraph(it: Iterable[Rdf#Triple]): Rdf#Graph

  def graphToIterable(graph: Rdf#Graph): Iterable[Rdf#Triple]

  // triple

  def makeTriple(s: Rdf#Node, p: Rdf#URI, o: Rdf#Node): Rdf#Triple

  def fromTriple(triple: Rdf#Triple): (Rdf#Node, Rdf#URI, Rdf#Node)

  // node

  def foldNode[T](node: Rdf#Node)(funURI: Rdf#URI => T, funBNode: Rdf#BNode => T, funLiteral: Rdf#Literal => T): T

  // URI

  def makeUri(s: String): Rdf#URI

  def fromUri(uri: Rdf#URI): String

  // bnode

  def makeBNode(): Rdf#BNode

  def makeBNodeLabel(s: String): Rdf#BNode

  def fromBNode(bn: Rdf#BNode): String

  // literal

  def foldLiteral[T](literal: Rdf#Literal)(funTL: Rdf#TypedLiteral => T, funLL: Rdf#LangLiteral => T): T

  // typed literal

  def makeTypedLiteral(lexicalForm: String, datatype: Rdf#URI): Rdf#TypedLiteral

  def fromTypedLiteral(tl: Rdf#TypedLiteral): (String, Rdf#URI)

  // lang literal

  def makeLangLiteral(lexicalForm: String, lang: Rdf#Lang): Rdf#LangLiteral

  def fromLangLiteral(ll: Rdf#LangLiteral): (String, Rdf#Lang)

  // lang

  def makeLang(s: String): Rdf#Lang

  def fromLang(l: Rdf#Lang): String

  // graph traversal

  def ANY: Rdf#NodeAny

  implicit def toConcreteNodeMatch(node: Rdf#Node): Rdf#NodeMatch

  def foldNodeMatch[T](nodeMatch: Rdf#NodeMatch)(funANY: => T, funNode: Rdf#Node => T): T

  def find(graph: Rdf#Graph, subject: Rdf#NodeMatch, predicate: Rdf#NodeMatch, objectt: Rdf#NodeMatch): Iterator[Rdf#Triple]

  // graph union
  def union(graphs: List[Rdf#Graph]): Rdf#Graph

  // graph isomorphism
  def isomorphism(left: Rdf#Graph, right: Rdf#Graph): Boolean

  // implicits

  implicit def graphSyntax(graph: Rdf#Graph): syntax.GraphSyntax[Rdf] = new syntax.GraphSyntax[Rdf](graph)

  implicit def tripleMatchSyntax(tripleMatch: TripleMatch[Rdf]): syntax.TripleMatchSyntax[Rdf] = new syntax.TripleMatchSyntax[Rdf](tripleMatch)

  implicit def tripleSyntax(triple: Rdf#Triple): syntax.TripleSyntax[Rdf] = new syntax.TripleSyntax[Rdf](triple)

  implicit def nodeMatchSyntax(nodeMatch: Rdf#NodeMatch): syntax.NodeMatchSyntax[Rdf] = new syntax.NodeMatchSyntax[Rdf](nodeMatch)

  implicit def nodeSyntax(node: Rdf#Node): syntax.NodeSyntax[Rdf] = new syntax.NodeSyntax[Rdf](node)

  implicit def uriSyntax(uri: Rdf#URI): syntax.URISyntax[Rdf] = new syntax.URISyntax[Rdf](uri)

  implicit def literalSyntax(literal: Rdf#Literal): syntax.LiteralSyntax[Rdf] = new syntax.LiteralSyntax[Rdf](literal)

  implicit def typedLiteralSyntax(tl: Rdf#TypedLiteral): syntax.TypedLiteralSyntax[Rdf] = new syntax.TypedLiteralSyntax[Rdf](tl)

  implicit def langLiteralSyntax(ll: Rdf#LangLiteral): syntax.LangLiteralSyntax[Rdf] = new syntax.LangLiteralSyntax[Rdf](ll)

  implicit def stringSyntax(s: String): syntax.StringSyntax = new syntax.StringSyntax(s)

  implicit def anySyntax[T](t: T): syntax.AnySyntax[Rdf, T] = new syntax.AnySyntax[Rdf, T](t)

  implicit def sparqlSolutionSyntax(solution: Rdf#Solution): syntax.SparqlSolutionSyntax[Rdf] = new syntax.SparqlSolutionSyntax[Rdf](solution)

  implicit def sparqlSolutionsSyntax(solutions: Rdf#Solutions): syntax.SparqlSolutionsSyntax[Rdf] = new syntax.SparqlSolutionsSyntax[Rdf](solutions)

  implicit def toPointedGraphW(node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraphW[Rdf] = new PointedGraphW[Rdf](PointedGraph(node)(ops))

}

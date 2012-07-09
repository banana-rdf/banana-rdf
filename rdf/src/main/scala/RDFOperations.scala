package org.w3.banana

/**
 * A Module that gathers the types needed to define an RDF implementation
 * Closely based on
 *   http://dvcs.w3.org/hg/rdf/raw-file/default/rdf-concepts/index.html
 * But with the  notable exceptions:
 *   - we allow literals in subject position - for help later in reasoning.
 */
trait RDFOperations[Rdf <: RDF] {

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

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#URI): Iterable[Rdf#Node]

  def getPredicates(graph: Rdf#Graph, subject: Rdf#Node): Iterable[Rdf#URI]

  def getSubjects(graph: Rdf#Graph, predicate: Rdf#URI, obj: Rdf#Node): Iterable[Rdf#Node]

  // graph union
  def union(graphs: Traversable[Rdf#Graph]): Rdf#Graph

  // graph isomorphism
  def isomorphism(left: Rdf#Graph, right: Rdf#Graph): Boolean

}

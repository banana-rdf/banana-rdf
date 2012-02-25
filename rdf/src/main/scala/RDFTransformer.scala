package org.w3.rdf

class RDFTransformer[A <: RDF, B <: RDF](
    val a: RDFOperations[A],
    val b: RDFOperations[B]) {

  def transform(graph: A#Graph): B#Graph =
    b.Graph(a.Graph.toIterable(graph) map transformTriple)
    
  def transformTriple(t: A#Triple): B#Triple = {
    val a.Triple(s, a.IRI(iri), o) = t
    b.Triple(
      transformNode(s),
      b.IRI(iri),
      transformNode(o))
  }
  
  def transformNode(n: A#Node): B#Node = a.Node.fold(n) (
    { case a.IRI(iri) => b.IRI(iri) },
    { case a.BNode(label) => b.BNode(label) },
    { literal: a.Literal => transformLiteral(literal) }
  )
  
  def transformLiteral(literal: A#Literal): B#Literal = a.Literal.fold(literal) (
    { case a.TypedLiteral(lexicalForm, a.IRI(datatypeIRI)) => b.TypedLiteral(lexicalForm, b.IRI(datatypeIRI)) },
    { case a.LangLiteral(lexicalForm, a.Lang(lang)) => b.LangLiteral(lexicalForm, b.Lang(lang)) }
  )
  
}

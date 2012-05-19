package org.w3.banana

class RDFTransformer[A <: RDF, B <: RDF](
    val a: RDFOperations[A],
    val b: RDFOperations[B]) {

  def transform(graph: A#Graph): B#Graph =
    b.Graph(a.Graph.toIterable(graph) map transformTriple)
    
  def transformTriple(t: A#Triple): B#Triple = {
    val a.Triple(s, a.URI(iri), o) = t
    b.Triple(
      transformNode(s),
      b.URI(iri),
      transformNode(o))
  }
  
  def transformNode(n: A#Node): B#Node = a.Node.fold(n) (
    { case a.URI(iri) => b.URI(iri) },
    { case a.BNode(label) => b.BNode(label) },
    { literal: A#Literal => transformLiteral(literal) }
  )
  
  def transformLiteral(literal: A#Literal): B#Literal = a.Literal.fold(literal) (
    { case a.TypedLiteral(lexicalForm, a.URI(datatypeURI)) => b.TypedLiteral(lexicalForm, b.URI(datatypeURI)) },
    { case a.LangLiteral(lexicalForm, a.Lang(lang)) => b.LangLiteral(lexicalForm, b.Lang(lang)) }
  )
  
}

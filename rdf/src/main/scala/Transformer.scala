package org.w3.rdf

class Transformer[ModelA <: RDFModule, ModelB <: RDFModule](val a: ModelA, val b: ModelB) {

  def transform(graph: a.Graph): b.Graph =
    b.Graph(graph map transformTriple)
    
  def transformTriple(t: a.Triple): b.Triple = {
    val a.Triple(s, a.IRI(iri), o) = t
    b.Triple(
      transformNode(s),
      b.IRI(iri),
      transformNode(o))
  }
  
  def transformNode(n: a.Node): b.Node = a.Node.fold(n) (
    { case a.IRI(iri) => b.IRI(iri) },
    { case a.BNode(label) => b.BNode(label) },
    { literal: a.Literal => transformLiteral(literal) }
  )
  
  def transformLiteral(literal: a.Literal): b.Literal = a.Literal.fold(literal) (
    { case a.TypedLiteral(lexicalForm, a.IRI(datatypeIRI)) => b.TypedLiteral(lexicalForm, b.IRI(datatypeIRI)) },
    { case a.LangLiteral(lexicalForm, a.Lang(lang)) => b.LangLiteral(lexicalForm, b.Lang(lang)) }
  )
  
}

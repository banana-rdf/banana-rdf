package org.w3.rdf

class Transformer[ModelA <: Module, ModelB <: Module](val a: ModelA, val b: ModelB) {

  def transform(graph: a.Graph): b.Graph =
    b.Graph(graph map transformTriple)
    
  def transformTriple(t: a.Triple): b.Triple = {
    val a.Triple(s, a.IRI(iri), o) = t
    b.Triple(
      transformNode(s),
      b.IRI(iri),
      transformNode(o))
  }
  
  def transformNode(n: a.Node): b.Node = n match {
    case a.IRI(iri) => b.IRI(iri)
    case a.BNode(label) => b.BNode(label)
    case a.Literal(literal,Right(a.IRI(datatype))) => b.Literal(literal,Right(b.IRI(datatype)))
    case a.Literal(literal,Left(a.Lang(tag))) => b.Literal(literal,Left(b.Lang(tag)))
  }
  
}

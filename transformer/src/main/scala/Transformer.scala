package org.w3.rdf

class Transformer[ModelA <: Model, ModelB <: Model](val a: ModelA, val b: ModelB) {

  def transform(graph: a.Graph): b.Graph =
    b.Graph(graph map transformTriple)
    
  def transformTriple(t: a.Triple): b.Triple = {
    val a.Triple(s, p, o) = t
    b.Triple(
      transformNode(s),
      transformIRI(p),
      transformNode(o))
  }
  
  def transformNode(n: a.Node): b.Node = n match {
    case a.NodeIRI(iri) => b.NodeIRI(transformIRI(iri))
    case a.NodeBNode(label) => b.NodeBNode(transformBNode(label))
    case a.NodeLiteral(literal) => b.NodeLiteral(transformLiteral(literal))
  }

  def transformIRI(iri: a.IRI): b.IRI = {
    val a.IRI(i) = iri
    b.IRI(i)
  }
  
  def transformBNode(bn: a.BNode): b.BNode = {
    val a.BNode(label) = bn
    b.BNode(label)
  }
  
  def transformLiteral(literal: a.Literal): b.Literal = {
    import a._
    val a.Literal(lit: String, langtagOption: Option[LangTag], datatypeOption: Option[IRI]) = literal
    b.Literal(
      lit,
      langtagOption map { case a.LangTag(lang) => b.LangTag(lang)},
      datatypeOption map transformIRI)
  }
  
}

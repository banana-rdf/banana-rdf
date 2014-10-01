package org.w3.banana

/**
 * class of Transformers between different RDF frameworks
 */
class RDFTransformer[A <: RDF, B <: RDF](
    val a: RDFOps[A],
    val b: RDFOps[B]) {

  def transform(graph: A#Graph): B#Graph =
    b.makeGraph(a.getTriples(graph).to[Iterable] map transformTriple)

  def transformTriple(triple: A#Triple): B#Triple = {
    val (s, p, o) = a.fromTriple(triple)
    val pString = a.fromUri(p)
    b.makeTriple(
      transformNode(s),
      b.makeUri(pString),
      transformNode(o))
  }

  def transformNode(node: A#Node): B#Node = a.foldNode(node)(
    uri => b.makeUri(a.fromUri(uri)),
    bnode => b.makeBNodeLabel(a.fromBNode(bnode)),
    {
      case a.Literal(lexicalForm, a.URI(datatype), None) => b.makeLiteral(lexicalForm, b.URI(datatype))
      case a.Literal(lexicalForm, _, Some(a.Lang(lang))) => b.makeLangTaggedLiteral(lexicalForm, b.Lang(lang))
    }
  )

}

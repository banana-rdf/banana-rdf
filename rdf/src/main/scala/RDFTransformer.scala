package org.w3.banana

/**
 * class of Transformers between different RDF frameworks
 */
class RDFTransformer[A <: RDF, B <: RDF](
    val a: RDFOperations[A],
    val b: RDFOperations[B]) {

  def transform(graph: A#Graph): B#Graph =
    b.makeGraph(a.graphToIterable(graph) map transformTriple)

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
    literal => transformLiteral(literal)
  )

  def transformLiteral(literal: A#Literal): B#Literal = a.foldLiteral(literal)(
    tl => {
      val (lexicalForm, datatypeUri) = a.fromTypedLiteral(tl)
      val datatype = a.fromUri(datatypeUri)
      b.makeTypedLiteral(lexicalForm, b.makeUri(datatype))
    },
    ll => {
      val (lexicalForm, lang) = a.fromLangLiteral(ll)
      val langString = a.fromLang(lang)
      b.makeLangLiteral(lexicalForm, b.makeLang(langString))
    }
  )

}

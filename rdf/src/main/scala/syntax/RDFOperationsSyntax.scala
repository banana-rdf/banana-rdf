package org.w3.banana.syntax

import org.w3.banana._

trait RDFOperationsSyntax[Rdf <: RDF] {

  def ops: RDFOperations[Rdf]

  // graph

  trait GraphCompanionObject {
    def empty: Rdf#Graph = ops.emptyGraph
    def apply(elems: Rdf#Triple*): Rdf#Graph = ops.makeGraph(elems.toIterable)
    def apply(it: Iterable[Rdf#Triple]): Rdf#Graph = ops.makeGraph(it)
  }

  object Graph extends GraphCompanionObject

  // triple

  trait TripleCompanionObject extends Function3[Rdf#Node, Rdf#URI, Rdf#Node, Rdf#Triple] {
    def apply(s: Rdf#Node, p: Rdf#URI, o: Rdf#Node): Rdf#Triple = ops.makeTriple(s, p, o)
    def unapply(triple: Rdf#Triple): Option[(Rdf#Node, Rdf#URI, Rdf#Node)] = Some(ops.fromTriple(triple))
  }

  object Triple extends TripleCompanionObject

  // URI

  trait URICompanionObject extends Function1[String, Rdf#URI] {
    def apply(s: String): Rdf#URI = ops.makeUri(s)
    def unapply(uri: Rdf#URI): Option[String] = Some(ops.fromUri(uri))
  }

  object URI extends URICompanionObject

  // alternative for building URIs
  // this feels more like a simple function than a constructor
  def uri(s: String): Rdf#URI = URI(s)

  // bnode

  trait BNodeCompanionObject extends Function1[String, Rdf#BNode] with Function0[Rdf#BNode] {
    def apply(): Rdf#BNode = ops.makeBNode()
    def apply(s: String): Rdf#BNode = ops.makeBNodeLabel(s)
    def unapply(bn: Rdf#BNode): Option[String] = Some(ops.fromBNode(bn))
  }

  object BNode extends BNodeCompanionObject

  // alternatives fr building bnodes
  def bnode(): Rdf#BNode = BNode()
  def bnode(s: String): Rdf#BNode = BNode(s)

  // typed literal

  trait TypedLiteralCompanionObject extends Function2[String, Rdf#URI, Rdf#TypedLiteral] with Function1[String, Rdf#TypedLiteral] {
    def unapply(tl: Rdf#TypedLiteral): Option[(String, Rdf#URI)] = Some(ops.fromTypedLiteral(tl))
    def apply(lexicalForm: String, datatype: Rdf#URI): Rdf#TypedLiteral = ops.makeTypedLiteral(lexicalForm, datatype)
    def apply(lexicalForm: String): Rdf#TypedLiteral = ops.makeTypedLiteral(lexicalForm, ops.makeUri("http://www.w3.org/2001/XMLSchema#string"))
  }

  object TypedLiteral extends TypedLiteralCompanionObject

  // lang literal

  trait LangLiteralCompanionObject extends Function2[String, Rdf#Lang, Rdf#LangLiteral] {
    def apply(lexicalForm: String, lang: Rdf#Lang): Rdf#LangLiteral = ops.makeLangLiteral(lexicalForm, lang)
    def unapply(ll: Rdf#LangLiteral): Option[(String, Rdf#Lang)] = Some(ops.fromLangLiteral(ll))
  }

  object LangLiteral extends LangLiteralCompanionObject

  // lang

  trait LangCompanionObject extends Function1[String, Rdf#Lang] {
    def apply(s: String): Rdf#Lang = ops.makeLang(s)
    def unapply(l: Rdf#Lang): Option[String] = Some(ops.fromLang(l))
  }

  object Lang extends LangCompanionObject

}

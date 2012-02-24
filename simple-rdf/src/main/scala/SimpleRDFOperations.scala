package org.w3.rdf.simple

import org.w3.rdf._

object SimpleRDFOperations extends RDFOperations[SimpleRDF] {
  
  object Graph extends GraphCompanionObject {
    def empty: Graph = SimpleModule.Graph.empty
    def apply(elems: Triple*): Graph = apply(elems.toIterable)
    def apply(it: Iterable[Triple]): Graph = SimpleModule.Graph.apply(it)
    def union(left: Graph, right: Graph): Graph = SimpleModule.Graph.union(left, right)
    def toIterable(graph: Graph): Iterable[Triple] = SimpleModule.Graph.toIterable(graph)
  }

  object Triple extends TripleCompanionObject {
    def apply(s: Node, p: IRI, o: Node) = SimpleModule.Triple(s, p, o)
    def unapply(t: Triple) = SimpleModule.Triple.unapply(t)
  }
  
  object Node extends NodeCompanionObject {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T) =
      SimpleModule.Node.fold(node)(funIRI, funBNode, funLiteral)
  }

  object IRI extends IRICompanionObject {
    def apply(s: String) = SimpleModule.IRI(s)
    def unapply(iri: IRI) = SimpleModule.IRI.unapply(iri)
  }

  object BNode extends BNodeCompanionObject {
    def apply(s: String) = SimpleModule.BNode(s)
    def unapply(bn: BNode) = SimpleModule.BNode.unapply(bn)
  }

  object Literal extends LiteralCompanionObject {
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T =
      SimpleModule.Literal.fold(literal)(funTL, funLL)
  }
  
  object TypedLiteral extends TypedLiteralCompanionObject {
    def apply(lexicalForm: String, datatype: IRI) = SimpleModule.TypedLiteral(lexicalForm, datatype)
    def unapply(tl: TypedLiteral) = SimpleModule.TypedLiteral.unapply(tl)
  }
  
  object LangLiteral extends LangLiteralCompanionObject {
    def apply(lexicalForm: String, lang: Lang) = SimpleModule.LangLiteral(lexicalForm, lang)
    def unapply(ll: LangLiteral) = SimpleModule.LangLiteral.unapply(ll)
  }

  object Lang extends LangCompanionObject {
    def apply(langString: String) = SimpleModule.Lang(langString)
    def unapply(lang: Lang) = SimpleModule.Lang.unapply(lang)
  }

}
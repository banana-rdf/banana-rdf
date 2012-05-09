package org.w3.banana.simple

import org.w3.banana._

object SimpleRDFOperations extends RDFOperations[SimpleRDF] {

  object Graph extends GraphCompanionObject {
    def empty: SimpleRDF#Graph = SimpleModule.Graph.empty
    def apply(elems: SimpleRDF#Triple*): SimpleRDF#Graph = apply(elems.toIterable)
    def apply(it: Iterable[SimpleRDF#Triple]): SimpleRDF#Graph = SimpleModule.Graph.apply(it)
    def toIterable(graph: SimpleRDF#Graph): Iterable[SimpleRDF#Triple] = SimpleModule.Graph.toIterable(graph)
  }

  object Triple extends TripleCompanionObject {
    def apply(s: SimpleRDF#Node, p: SimpleRDF#IRI, o: SimpleRDF#Node) = SimpleModule.Triple(s, p, o)
    def unapply(t: SimpleRDF#Triple) = SimpleModule.Triple.unapply(t)
  }
  
  object Node extends NodeCompanionObject {
    def fold[T](node: SimpleRDF#Node)(funIRI: SimpleRDF#IRI => T, funBNode: SimpleRDF#BNode => T, funLiteral: SimpleRDF#Literal => T) =
      SimpleModule.Node.fold(node)(funIRI, funBNode, funLiteral)
  }

  object IRI extends IRICompanionObject {
    def apply(s: String) = SimpleModule.IRI(s)
    def unapply(iri: SimpleRDF#IRI) = SimpleModule.IRI.unapply(iri)
  }

  object BNode extends BNodeCompanionObject {
    def apply() = SimpleModule.BNode()
    def apply(s: String) = SimpleModule.BNode(s)
    def unapply(bn: SimpleRDF#BNode) = SimpleModule.BNode.unapply(bn)
  }

  object Literal extends LiteralCompanionObject {
    def fold[T](literal: SimpleRDF#Literal)(funTL: SimpleRDF#TypedLiteral => T, funLL: SimpleRDF#LangLiteral => T): T =
      SimpleModule.Literal.fold(literal)(funTL, funLL)
  }
  
  object TypedLiteral extends TypedLiteralCompanionObject {
    def apply(lexicalForm: String, datatype: SimpleRDF#IRI) = SimpleModule.TypedLiteral(lexicalForm, datatype)
    def unapply(tl: SimpleRDF#TypedLiteral) = SimpleModule.TypedLiteral.unapply(tl)
  }
  
  object LangLiteral extends LangLiteralCompanionObject {
    def apply(lexicalForm: String, lang: SimpleRDF#Lang) = SimpleModule.LangLiteral(lexicalForm, lang)
    def unapply(ll: SimpleRDF#LangLiteral) = SimpleModule.LangLiteral.unapply(ll)
  }

  object Lang extends LangCompanionObject {
    def apply(langString: String) = SimpleModule.Lang(langString)
    def unapply(lang: SimpleRDF#Lang) = SimpleModule.Lang.unapply(lang)
  }

}

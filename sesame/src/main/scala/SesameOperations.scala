package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.model.impl._
import org.openrdf.model._
import scala.collection.JavaConverters._

object SesameOperations extends RDFOperations[SesameDataType] {
  
  override type Graph = SesameDataType#Graph
  override type Triple = SesameDataType#Triple
  override type Node = SesameDataType#Node
  override type IRI = SesameDataType#IRI
  override type BNode = SesameDataType#BNode
  override type Literal = SesameDataType#Literal
  override type TypedLiteral = SesameDataType#TypedLiteral
  override type LangLiteral = SesameDataType#LangLiteral
  override type Lang = SesameDataType#Lang
  
  object Graph extends GraphCompanionObject {
    def empty: Graph = new GraphImpl
    def apply(elems: Triple*): Graph = apply(elems.toIterable)
    def apply(it: Iterable[Triple]): Graph = {
      val graph = empty
      it foreach { t => graph add t }
      graph
    }
    def union(left: Graph, right: Graph): Graph = {
      val graph = empty
      toIterable(left) foreach { t => graph add t }
      toIterable(right) foreach { t => graph add t }
      graph
    }
    def toIterable(graph: Graph): Iterable[Triple] = graph.asScala
  }
  
  object Triple extends TripleCompanionObject {
    def apply(s: Node, p: IRI, o: Node): Triple = { new StatementImpl(s.asInstanceOf[Resource], p, o)
//    s match {
//      case s: Resource => new Triple(s, p, o)
//      case _ => XXX what if the subject is a literal?
//    }
    }
    def unapply(t: Triple): Option[(Node, IRI, Node)] =
      (t.getSubject, t.getPredicate, t.getObject) match {
        case (s, p: IRI, o) => Some((s, p, o))
        case _ => None
      }
  }
  
  object Node extends NodeCompanionObject {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T = node match {
      case iri: IRI => funIRI(iri)
      case bnode: BNode => funBNode(bnode)
      case literal: Literal => funLiteral(literal)
    }
  }
  
  object IRI extends IRICompanionObject {
    def apply(iriStr: String): IRI = ValueFactoryImpl.getInstance.createURI(iriStr).asInstanceOf[IRI]
    def unapply(node: IRI): Option[String] = Some(node.toString)
  }
  
  object BNode extends BNodeCompanionObject {
    def apply(label: String): BNode = new BNodeImpl(label)
    def unapply(bn: BNode): Option[String] = Some(bn.getID)
  }
  
  object Literal extends LiteralCompanionObject {
    /**
     * LangLiteral are not different types in Jena
     * we can discriminate on the lang tag presence
     */
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T = 
    literal match {
      case typedLiteral: TypedLiteral if literal.getLanguage == null || literal.getLanguage.isEmpty =>
        funTL(typedLiteral)
      case langLiteral: LangLiteral => funLL(langLiteral)
    }
  }
  
  object TypedLiteral extends TypedLiteralCompanionObject {
    def apply(lexicalForm: String, iri: IRI): TypedLiteral = new LiteralImpl(lexicalForm, iri)
    def unapply(typedLiteral: TypedLiteral): Option[(String, IRI)] =
      (typedLiteral.getLabel, typedLiteral.getDatatype) match {
        case (lexicalForm: String, iri: IRI) if typedLiteral.getLanguage == null => {
          if (iri != null)
            Some(lexicalForm, iri)
          else
            Some(lexicalForm, xsdString)
        }
        case _ => None
      }
  }
  
  object LangLiteral extends LangLiteralCompanionObject {
    def apply(lexicalForm: String, lang: Lang): LangLiteral = {
      val Lang(langString) = lang
      new LiteralImpl(lexicalForm, langString)
    }
    def unapply(langLiteral: LangLiteral): Option[(String, Lang)] = {
      val l = langLiteral.getLanguage
      if (l != null && l != "")
        Some((langLiteral.getLabel, Lang(l)))
      else
        None
    }
  }
  
  object Lang extends LangCompanionObject {
    def apply(langString: String) = langString
    def unapply(lang: Lang) = Some(lang)
  }
}

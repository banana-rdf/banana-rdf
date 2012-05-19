package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model.impl._
import org.openrdf.model._
import scala.collection.JavaConverters._

import SesamePrefix._

object SesameOperations extends RDFOperations[Sesame] {
  
  object Graph extends GraphCompanionObject {
    def empty: Sesame#Graph = new GraphImpl
    def apply(elems: Sesame#Triple*): Sesame#Graph = apply(elems.toIterable)
    def apply(it: Iterable[Sesame#Triple]): Sesame#Graph = {
      val graph = empty
      it foreach { t => graph add t }
      graph
    }
    def union(left: Sesame#Graph, right: Sesame#Graph): Sesame#Graph = {
      val graph = empty
      toIterable(left) foreach { t => graph add t }
      toIterable(right) foreach { t => graph add t }
      graph
    }
    def toIterable(graph: Sesame#Graph): Iterable[Sesame#Triple] = graph.asScala
  }
  
  object Triple extends TripleCompanionObject {
    def apply(s: Sesame#Node, p: Sesame#URI, o: Sesame#Node): Sesame#Triple = { new StatementImpl(s.asInstanceOf[Resource], p, o)
//    s match {
//      case s: Resource => new Triple(s, p, o)
//      case _ => XXX what if the subject is a literal?
//    }
    }
    def unapply(t: Sesame#Triple): Option[(Sesame#Node, Sesame#URI, Sesame#Node)] =
      (t.getSubject, t.getPredicate, t.getObject) match {
        case (s, p: Sesame#URI, o) => Some((s, p, o))
        case _ => None
      }
  }
  
  object Node extends NodeCompanionObject {
    def fold[T](node: Sesame#Node)(funURI: Sesame#URI => T, funBNode: Sesame#BNode => T, funLiteral: Sesame#Literal => T): T = node match {
      case iri: Sesame#URI => funURI(iri)
      case bnode: Sesame#BNode => funBNode(bnode)
      case literal: Sesame#Literal => funLiteral(literal)
    }
  }
  
  object URI extends URICompanionObject {
    def apply(iriStr: String): Sesame#URI = ValueFactoryImpl.getInstance.createURI(iriStr).asInstanceOf[Sesame#URI]
    def unapply(node: Sesame#URI): Option[String] = Some(node.toString)
  }
  
  object BNode extends BNodeCompanionObject {

    def apply() = ValueFactoryImpl.getInstance().createBNode()
    def apply(label: String): Sesame#BNode = new BNodeImpl(label)
    def unapply(bn: Sesame#BNode): Option[String] = Some(bn.getID)
  }
  
  object Literal extends LiteralCompanionObject {
    /**
     * LangLiteral are not different types in Jena
     * we can discriminate on the lang tag presence
     */
    def fold[T](literal: Sesame#Literal)(funTL: Sesame#TypedLiteral => T, funLL: Sesame#LangLiteral => T): T = 
    literal match {
      case typedLiteral: Sesame#TypedLiteral if literal.getLanguage == null || literal.getLanguage.isEmpty =>
        funTL(typedLiteral)
      case langLiteral: Sesame#LangLiteral => funLL(langLiteral)
    }
  }
  
  object TypedLiteral extends TypedLiteralCompanionObject {
    def apply(lexicalForm: String, iri: Sesame#URI): Sesame#TypedLiteral = new LiteralImpl(lexicalForm, iri)
    def unapply(typedLiteral: Sesame#TypedLiteral): Option[(String, Sesame#URI)] =
      (typedLiteral.getLabel, typedLiteral.getDatatype) match {
        case (lexicalForm: String, iri: Sesame#URI) if typedLiteral.getLanguage == null => {
          if (iri != null)
            Some(lexicalForm, iri)
          else
            Some(lexicalForm, xsd.string)
        }
        case _ => None
      }
  }
  
  object LangLiteral extends LangLiteralCompanionObject {
    def apply(lexicalForm: String, lang: Sesame#Lang): Sesame#LangLiteral = {
      val Lang(langString) = lang
      new LiteralImpl(lexicalForm, langString)
    }
    def unapply(langLiteral: Sesame#LangLiteral): Option[(String, Sesame#Lang)] = {
      val l = langLiteral.getLanguage
      if (l != null && l != "")
        Some((langLiteral.getLabel, Lang(l)))
      else
        None
    }
  }
  
  object Lang extends LangCompanionObject {
    def apply(langString: String) = langString
    def unapply(lang: Sesame#Lang) = Some(lang)
  }
}

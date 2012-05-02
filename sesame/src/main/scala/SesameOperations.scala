package org.w3.rdf.sesame

import org.w3.rdf._
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
    def apply(s: Sesame#Node, p: Sesame#IRI, o: Sesame#Node): Sesame#Triple = { new StatementImpl(s.asInstanceOf[Resource], p, o)
//    s match {
//      case s: Resource => new Triple(s, p, o)
//      case _ => XXX what if the subject is a literal?
//    }
    }
    def unapply(t: Sesame#Triple): Option[(Sesame#Node, Sesame#IRI, Sesame#Node)] =
      (t.getSubject, t.getPredicate, t.getObject) match {
        case (s, p: Sesame#IRI, o) => Some((s, p, o))
        case _ => None
      }
  }
  
  object Node extends NodeCompanionObject {
    def fold[T](node: Sesame#Node)(funIRI: Sesame#IRI => T, funBNode: Sesame#BNode => T, funLiteral: Sesame#Literal => T): T = node match {
      case iri: Sesame#IRI => funIRI(iri)
      case bnode: Sesame#BNode => funBNode(bnode)
      case literal: Sesame#Literal => funLiteral(literal)
    }
  }
  
  object IRI extends IRICompanionObject {
    def apply(iriStr: String): Sesame#IRI = ValueFactoryImpl.getInstance.createURI(iriStr).asInstanceOf[Sesame#IRI]
    def unapply(node: Sesame#IRI): Option[String] = Some(node.toString)
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
    def apply(lexicalForm: String, iri: Sesame#IRI): Sesame#TypedLiteral = new LiteralImpl(lexicalForm, iri)
    def unapply(typedLiteral: Sesame#TypedLiteral): Option[(String, Sesame#IRI)] =
      (typedLiteral.getLabel, typedLiteral.getDatatype) match {
        case (lexicalForm: String, iri: Sesame#IRI) if typedLiteral.getLanguage == null => {
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

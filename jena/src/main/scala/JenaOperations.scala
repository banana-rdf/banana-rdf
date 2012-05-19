package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model.AnonId
import com.hp.hpl.jena.datatypes.TypeMapper
import scala.collection.JavaConverters._

import JenaPrefix._

object JenaOperations extends RDFOperations[Jena] {

  object Graph extends GraphCompanionObject {
    def empty: Jena#Graph = Factory.createDefaultGraph
    def apply(elems: Jena#Triple*): Jena#Graph = apply(elems.toIterable)
    def apply(it: Iterable[Jena#Triple]): Jena#Graph = {
      val graph = empty
      it foreach { t => graph add t }
      graph
    }
    def toIterable(graph: Jena#Graph): Iterable[Jena#Triple] = new Iterable[Jena#Triple] {
      val iterator = graph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY).asScala
    }
  }

  object Triple extends TripleCompanionObject {
    def apply(s: Jena#Node, p: Jena#URI, o: Jena#Node): Jena#Triple = {
      JenaTriple.create(s, p, o)
    }
    def unapply(t: Jena#Triple): Option[(Jena#Node, Jena#URI, Jena#Node)] =
      (t.getSubject, t.getPredicate, t.getObject) match {
        case (s, p: Jena#URI, o) => Some((s, p, o))
        case _ => None
      }
  }

  object Node extends NodeCompanionObject {
    def fold[T](node: Jena#Node)(funURI: Jena#URI => T, funBNode: Jena#BNode => T, funLiteral: Jena#Literal => T): T = node match {
      case iri: Jena#URI => funURI(iri)
      case bnode: Jena#BNode => funBNode(bnode)
      case literal: Jena#Literal => funLiteral(literal)
    }
  }
  
  object URI extends URICompanionObject {
    def apply(iriStr: String): Jena#URI = { JenaNode.createURI(iriStr).asInstanceOf[Node_URI] }
    def unapply(node: Jena#URI): Option[String] = if (node.isURI) Some(node.getURI) else None
  }

  object BNode extends BNodeCompanionObject {
    def apply() = JenaNode.createAnon().asInstanceOf[Node_Blank]
    def apply(label: String): Jena#BNode = {
      val id = AnonId.create(label)
      JenaNode.createAnon(id).asInstanceOf[Node_Blank]
    }
    def unapply(bn: Jena#BNode): Option[String] =
      if (bn.isBlank) Some(bn.getBlankNodeId.getLabelString) else None

  }

  lazy val mapper = TypeMapper.getInstance
  def jenaDatatype(datatype: Jena#URI) = {
    val URI(iriString) = datatype
    mapper.getTypeByName(iriString)
  }
  
  object Literal extends LiteralCompanionObject {
    /**
     * LangLiteral are not different types in Jena
     * we can discriminate on the lang tag presence
     */
    def fold[T](literal: Jena#Literal)(funTL: Jena#TypedLiteral => T, funLL: Jena#LangLiteral => T): T = literal match {
      case typedLiteral: Jena#TypedLiteral if literal.getLiteralLanguage == null || literal.getLiteralLanguage.isEmpty =>
        funTL(typedLiteral)
      case langLiteral: Jena#LangLiteral => funLL(langLiteral)
    }
  }

  object TypedLiteral extends TypedLiteralCompanionObject {
    def apply(lexicalForm: String, iri: Jena#URI): Jena#TypedLiteral = {
      JenaNode.createLiteral(lexicalForm, null, jenaDatatype(iri)).asInstanceOf[Node_Literal]
    }
    def unapply(typedLiteral: Jena#TypedLiteral): Option[(String, Jena#URI)] = {
      val typ = typedLiteral.getLiteralDatatype
      if (typ != null)
        Some((typedLiteral.getLiteralLexicalForm.toString, URI(typ.getURI)))
      else if (typedLiteral.getLiteralLanguage.isEmpty)
        Some((typedLiteral.getLiteralLexicalForm.toString, xsd.string))
      else
        None
    }
  }
  
  object LangLiteral extends LangLiteralCompanionObject {
    def apply(lexicalForm: String, lang: Jena#Lang): Jena#LangLiteral = {
      val Lang(langString) = lang
      JenaNode.createLiteral(lexicalForm, langString, null).asInstanceOf[Node_Literal]
    }
    def unapply(langLiteral: Jena#LangLiteral): Option[(String, Jena#Lang)] = {
      val l = langLiteral.getLiteralLanguage
      if (l != "")
        Some((langLiteral.getLiteralLexicalForm.toString, Lang(l)))
      else
        None
    }
  }
  
  object Lang extends LangCompanionObject {
    def apply(langString: String) = langString
    def unapply(lang: Jena#Lang) = Some(lang)
  }
}

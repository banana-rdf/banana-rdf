package org.w3.rdf.jena

import org.w3.rdf._
import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model.AnonId
import com.hp.hpl.jena.datatypes.TypeMapper

import org.w3.algebraic._

object JenaModule extends Module {

  class Graph(val jenaGraph: JenaGraph) extends GraphInterface {
    def iterator: Iterator[Triple] = new Iterator[Triple] {
      val iterator = jenaGraph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY)
      def hasNext = iterator.hasNext
      def next = iterator.next
    }
    def ++(other: Graph):Graph = {
      val g = Factory.createDefaultGraph
      iterator foreach { t => g add t }
      other.iterator foreach { t => g add t }
      new Graph(g)
    }

  }

  object Graph extends GraphCompanionObject {
    def fromJena(jenaGraph: JenaGraph): Graph = new Graph(jenaGraph)
    def empty: Graph = new Graph(Factory.createDefaultGraph)
    def apply(elems: Triple*): Graph = apply(elems.toIterable)
    def apply(it: Iterable[Triple]): Graph = {
      val jenaGraph = Factory.createDefaultGraph
      it foreach { t => jenaGraph add t }
      new Graph(jenaGraph)
    }
  }

  type Triple = JenaTriple
  object Triple extends TripleCompanionObject {
    def apply(s: Node, p: IRI, o: Node): Triple = {
      JenaTriple.create(s, p, o)
    }
    def unapply(t: Triple): Option[(Node, IRI, Node)] =
      (t.getSubject, t.getPredicate, t.getObject) match {
        case (s, p: IRI, o) => Some((s, p, o))
        case _ => None
      }
  }

  type Node = JenaNode
  
  object Node extends NodeCompanionObject {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T = node match {
      case iri: IRI => funIRI(iri)
      case bnode: BNode => funBNode(bnode)
      case literal: Literal => funLiteral(literal)
    }
  }
  
  type IRI = Node_URI
  object IRI extends IRICompanionObject {
    def apply(iriStr: String): IRI = { JenaNode.createURI(iriStr).asInstanceOf[Node_URI] }
    def unapply(node: IRI): Option[String] = if (node.isURI) Some(node.getURI) else None
  }

  type BNode = Node_Blank
  object BNode extends BNodeCompanionObject {
    def apply(label: String): BNode = {
      val id = AnonId.create(label)
      JenaNode.createAnon(id).asInstanceOf[Node_Blank]
    }
    def unapply(bn: BNode): Option[String] =
      if (bn.isBlank) Some(bn.getBlankNodeId.getLabelString) else None
  }

  lazy val mapper = TypeMapper.getInstance
  def jenaDatatype(datatype: IRI) = {
    val IRI(iriString) = datatype
    mapper.getTypeByName(iriString)
  }
  
  type Literal = Node_Literal
  
  object Literal extends LiteralCompanionObject {
    /**
     * LangLiteral are not different types in Jena
     * we can discriminate on the lang tag presence
     */
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T = literal match {
      case typedLiteral: TypedLiteral if literal.getLiteralLanguage == null || literal.getLiteralLanguage.isEmpty =>
        funTL(typedLiteral)
      case langLiteral: LangLiteral => funLL(langLiteral)
    }
  }

  
  type TypedLiteral = Node_Literal
  object TypedLiteral extends TypedLiteralCompanionObject {
    def apply(lexicalForm: String, iri: IRI): TypedLiteral = {
      val IRI(iriString) = iri
      val typ = mapper.getTypeByName(iriString)
      JenaNode.createLiteral(lexicalForm, null, typ).asInstanceOf[Node_Literal]
    }
    def unapply(typedLiteral: TypedLiteral): Option[(String, IRI)] = {
      val typ = typedLiteral.getLiteralDatatype
      if (typ != null)
        Some((typedLiteral.getLiteralLexicalForm.toString, IRI(typ.getURI)))
      else if (typedLiteral.getLiteralLanguage.isEmpty)
        Some((typedLiteral.getLiteralLexicalForm.toString, xsdString))
      else
        None
    }
  }
  
  type LangLiteral = Node_Literal
  object LangLiteral extends LangLiteralCompanionObject {
    def apply(lexicalForm: String, lang: Lang): LangLiteral = {
      val Lang(langString) = lang
      JenaNode.createLiteral(lexicalForm, langString, jenaDatatype(xsdString)).asInstanceOf[Node_Literal]
    }
    def unapply(langLiteral: LangLiteral): Option[(String, Lang)] = {
      val l = langLiteral.getLiteralLanguage
      if (l != "")
        Some((langLiteral.getLiteralLexicalForm.toString, Lang(l)))
      else
        None
    }
  }
  
  type Lang = String
  object Lang extends LangCompanionObject {
    def apply(langString: String) = langString
    def unapply(lang: Lang) = Some(lang)
  }
}

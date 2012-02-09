package org.w3.rdf.jena

import org.w3.rdf._
import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model.{AnonId}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}

import org.w3.algebraic._
import com.hp.hpl.jena.vocabulary.{RDF, XSD}
import util.MurmurHash3

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
  object Triple extends AlgebraicDataType3[Node, IRI, Node, Triple] {
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
  object Node {
    def unapply(node: JenaNode): Option[Node] =
      if (node.isURI || node.isBlank || node.isLiteral) Some(node) else None
  }

  type IRI = JenaNode
  object IRI extends AlgebraicDataType1[String, IRI]  {
    def apply(iriStr: String): IRI = { JenaNode.createURI(iriStr).asInstanceOf[Node_URI] }
    def unapply(node: IRI): Option[String] = if (node.isURI) Some(node.getURI) else None
  }

  type BNode = JenaNode
  object BNode extends AlgebraicDataType1[String, BNode] {
    def apply(label: String): BNode = {
      val id = AnonId.create(label)
      JenaNode.createAnon(id).asInstanceOf[Node_Blank]
    }
    def unapply(bn: BNode): Option[String] =
      if (bn.isBlank) Some(bn.getBlankNodeId.getLabelString) else None
  }

  lazy val mapper = TypeMapper.getInstance
  
  type Literal = JenaNode
  object Literal extends AlgebraicDataType2[String, IRI, Literal] with Function1[String, Literal] {
    def apply(lexicalForm: String) = apply(lexicalForm, xsdStringIRI)

    def apply(lexicalForm: String, dataType: IRI) = {
      dataType match {
        case Lang(tag) => JenaNode.createLiteral(lexicalForm, tag, null)
        case IRI(iri) => JenaNode.createLiteral(lexicalForm, null,
          if (iri eq xsdString) null else mapper.getTypeByName(iri))
      }
    }

    def unapply(literal: Literal): Option[(String, IRI)] = {
      val lang = literal.getLiteralLanguage;
      val lexicalForm = literal.getLiteralLexicalForm
      if ("" != lang) Some(lexicalForm, Lang(lang))
      else {
        val dtIRI = literal.getLiteralDatatype
        val dataType = if (null == dtIRI) xsdStringIRI else IRI(dtIRI.getURI)
        Some((lexicalForm, dataType))
      }
    }
  }




  //IRI should be composed of pieces, then tag can be a part of IRI, perhaps a  the rdfLang+"_"+tag be
  //the full IRI
  class Lang(val tag: String) extends Node_URI(rdfLang) {
    override def equals(obj: Any) = obj match {
      case otherlang: Lang => otherlang.tag == tag
      case _ => false
    }
    override lazy val hashCode = MurmurHash3.stringHash(tag)
  }

  object Lang extends AlgebraicDataType1[String, Lang] {
    def unapply(lt: Lang): Option[String] = if (lt.tag != null) return Some(lt.tag) else None
    def apply(tag: String): Lang = new Lang(tag)
  }
}

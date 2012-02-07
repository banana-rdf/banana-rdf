package org.w3.rdf.jena

import org.w3.rdf._
import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.rdf.model.{AnonId}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}

import org.w3.algebraic._
import com.hp.hpl.jena.vocabulary.{RDF, XSD}

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

//  type NodeIRI = JenaNode
//  object NodeIRI extends AlgebraicDataType1[IRI, NodeIRI] {
//    def apply(iri: IRI): NodeIRI = { val IRI(s) = iri ; JenaNode.createURI(s).asInstanceOf[Node_URI] }
//    def unapply(node: NodeIRI): Option[IRI] = if (node.isURI) Some(IRI(node.getURI)) else None
//  }
//
//  type NodeBNode = JenaNode
//  object NodeBNode extends AlgebraicDataType1[BNode, NodeBNode] {
//    def apply(node: BNode): NodeBNode = node
//    def unapply(node: NodeBNode): Option[BNode] = if (node.isBlank) Some(node.asInstanceOf[Node_Blank]) else None
//  }
//
//  type NodeLiteral = JenaNode
//  object NodeLiteral extends AlgebraicDataType1[Literal, NodeLiteral] {
//    def apply(literal: Literal): NodeLiteral = literal
//    def unapply(node: NodeLiteral): Option[Literal] =
//      if (node.isLiteral) Some(node.asInstanceOf[Node_Literal]) else None
//  }

  type IRI = Node_URI
  object IRI extends AlgebraicDataType1[String, IRI]  {
    def apply(iriStr: String): IRI = { JenaNode.createURI(iriStr).asInstanceOf[Node_URI] }
    def unapply(node: IRI): Option[String] = if (node.isURI) Some(node.getURI) else None
  }

  type BNode = Node_Blank
  object BNode extends AlgebraicDataType1[String, BNode] {
    def apply(label: String): BNode = {
      val id = AnonId.create(label)
      JenaNode.createAnon(id).asInstanceOf[Node_Blank]
    }
    def unapply(bn: BNode): Option[String] =
      if (bn.isBlank) Some(bn.getBlankNodeId.getLabelString) else None
  }

  lazy val mapper = TypeMapper.getInstance
  
  type Literal = Node_Literal
  object Literal extends AlgebraicDataType2[String, IRI, Literal] {
    def apply(lit: String, datatype: IRI=xsdString): Literal = {
      val node = datatype match {
        case lang: LangTag => JenaNode.createLiteral(lit,lang.s,null)
        case _ => JenaNode.createLiteral(lit,null, mapper.getTypeByName(datatype.getURI))
      }
      node.asInstanceOf[Node_Literal]
    }
    
    def unapply(literal: Literal): Option[(String, IRI)] =
      if (literal.isLiteral)
        Some((
          literal.getLiteralLexicalForm.toString,
          { val l = literal.getLiteralLanguage; 
            if (l != "") LangTag(l) else IRI(literal.getLiteralDatatype.getURI)})
        )
      else
        None
  }
  
  case class LangTag(s: String) extends IRI {
    override lazy val getURI = RDF.getURI+s

    override def equals(that: Any) = that match {
      case lt: LangTag => lt.s == s
      case IRI(iri) => iri == getURI
    }

    override def hashCode() = getURI.hashCode()
  }
  object LangTag extends AlgebraicDataType1[String, LangTag]

}

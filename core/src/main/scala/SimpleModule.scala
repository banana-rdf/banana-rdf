package org.w3.rdf

import org.w3.algebraic._
import util.{MurmurHash3, MurmurHash}

object ScalaModule extends Module {


  case class Graph(triples: Set[Triple]) extends GraphInterface {
    def iterator = triples.iterator
    def ++(other: Graph): Graph = Graph(triples ++ other.triples)
  }

  object Graph extends GraphCompanionObject {
    def empty: Graph = Graph(Set[Triple]())
    def apply(elems: Triple*): Graph = Graph(Set[Triple](elems:_*))
    def apply(it: Iterable[Triple]): Graph = Graph(it.toSet)
  }

  case class Triple (s: Node, p: IRI, o: Node)
  object Triple extends AlgebraicDataType3[Node, IRI, Node, Triple]

  sealed trait Node

  trait IRIWA extends Node {
    val iri: String
   }
  type IRIWorkAround = IRIWA
  case class IRI(iri: String) extends IRIWA { override def toString = '<' + iri + '>' }
  object IRI extends AlgebraicDataType1[String, IRI]

  case class BNode(label: String) extends Node
  object BNode extends AlgebraicDataType1[String, BNode]

  case class Literal protected (lexicalForm: String, dataType: IRIWorkAround) extends Node
  object Literal extends AlgebraicDataType2[String, IRIWorkAround, Literal] with Function1[String, Literal] {
    def apply(lexicalForm: String) = apply(lexicalForm, xsdStringIRI)
  }

  //IRI should be composed of pieces, then tag can be a part of IRI, perhaps a  the rdfLang+"_"+tag be
  //the full IRI
  case class Lang(val tag: String) extends IRIWA {
    val iri = rdfLang
    override def toString = "@"+tag
  }
  object Lang extends AlgebraicDataType1[String, Lang]

}

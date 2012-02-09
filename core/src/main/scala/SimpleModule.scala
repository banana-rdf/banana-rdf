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

  case class IRI(iri: String) extends Node { override def toString = '<' + iri + '>' }
  object IRI extends AlgebraicDataType1[String, IRI]

  case class BNode(label: String) extends Node
  object BNode extends AlgebraicDataType1[String, BNode]

  case class Literal protected (lexicalForm: String, dataType: IRI) extends Node
  object Literal extends AlgebraicDataType2[String, IRI, Literal] with Function1[String, Literal] {
    def apply(lexicalForm: String) = apply(lexicalForm, xsdStringIRI)
  }

  //IRI should be composed of pieces, then tag can be a part of IRI, perhaps a  the rdfLang+"_"+tag be
  //the full IRI
  class Lang(val tag: String) extends IRI(rdfLang) {
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

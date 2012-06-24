package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model.{ Graph => SesameGraph, Literal => SesameLiteral, BNode => SesameBNode, URI => SesameURI, _ }
import org.openrdf.repository._

trait Sesame extends RDF {
  type Graph = SesameGraph
  type Triple = Statement
  type Node = Value
  type URI = SesameURI
  type BNode = SesameBNode
  type Literal = SesameLiteral
  type TypedLiteral = SesameLiteral
  type LangLiteral = SesameLiteral
  type Lang = String
}

object Sesame {

  implicit val ops: RDFOperations[Sesame] = SesameOperations

  implicit val diesel: Diesel[Sesame] = Diesel[Sesame]

  implicit val rdfxmlReader: RDFReader[Sesame, RDFXML] = SesameRDFXMLReader

  implicit val sparqlOps: SPARQLOperations[Sesame, SesameSPARQL] = SesameSPARQLOperations

  implicit val graphQuery: RDFGraphQuery[Sesame, SesameSPARQL] = SesameGraphQuery

}

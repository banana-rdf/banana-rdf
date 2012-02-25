package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.model.impl._
import org.openrdf.model._
import scala.collection.JavaConverters._

trait Sesame extends RDF {
  type Graph = GraphImpl
  type Triple = Statement
  type Node = Value
  type IRI = URIImpl
  type BNode = BNodeImpl
  type Literal = LiteralImpl
  type TypedLiteral = LiteralImpl
  type LangLiteral = LiteralImpl
  type Lang = String
}

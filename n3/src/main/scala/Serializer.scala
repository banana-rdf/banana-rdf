/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

import org.w3.rdf._

/**
 * Async Parser for the simplest of all RDF encodings: NTriples
 * http://www.w3.org/TR/rdf-testcases/#ntriples
 *
 * This is using the nomo library that is being developed
 * here:  https://bitbucket.org/pchiusano/nomo
 *
 * @author bblfish
 * @since 02/02/2012
 */

class Serializer[Rdf <: RDF](ops: RDFOperations[Rdf]) {
  
  import ops._
  
//  val pimps = new Pimps(m)
//  import pimps._
  
  def asN3(graph: Graph): String =
    graph map tripleAsN3 mkString "\n"

  def tripleAsN3(triple: Triple): String = {
    val Triple(s, p, o) = triple
    "%s %s %s ." format (nodeAsN3(s), iriAsN3(p), nodeAsN3(o))
  }
  
  def nodeAsN3(node: Node): String = Node.fold(node) (
    iriAsN3,
    { case BNode(bnode) => "_:" + bnode },
    { l: Literal => literalAsN3(l) }
  )
  
  def iriAsN3(iri: IRI): String = {
    val IRI(iriString) = iri
    "<" + NTriplesParser.toIRI(iriString) + ">"
  }
  
  def literalAsN3(literal: Literal): String = Literal.fold(literal) (
    { typedLiteral: TypedLiteral => typedLiteralAsN3(typedLiteral) },
    { case LangLiteral(lexicalForm, Lang(lang)) => "\"%s\"@%s" format (NTriplesParser.toAsciiLiteral(lexicalForm), lang) }
  )
  
  def typedLiteralAsN3(typedLiteral: TypedLiteral): String = typedLiteral match {
    case TypedLiteral(lexicalForm, datatype) if datatype == xsdString =>
      "\"%s\"" format NTriplesParser.toAsciiLiteral(lexicalForm)
    case TypedLiteral(lexicalForm, IRI(iri)) =>
      "\"%s\"^^<%s>" format (NTriplesParser.toAsciiLiteral(lexicalForm), NTriplesParser.toIRI(iri))
  }
  
}

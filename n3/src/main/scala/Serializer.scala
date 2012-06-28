/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.banana.n3

import org.w3.banana._

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

class Serializer[Rdf <: RDF](diesel: Diesel[Rdf]) {

  import diesel._
  import ops._

  def asN3(graph: Rdf#Graph): String =
    graphToIterable(graph) map tripleAsN3 mkString "\n"

  def tripleAsN3(triple: Rdf#Triple): String = {
    val (s, p, o) = fromTriple(triple)
    "%s %s %s ." format (nodeAsN3(s), iriAsN3(p), nodeAsN3(o))
  }

  def nodeAsN3(node: Rdf#Node): String = foldNode(node)(
    iriAsN3,
    bnode => "_:" + fromBNode(bnode),
    literal => literalAsN3(literal)
  )

  def iriAsN3(iri: Rdf#URI): String = {
    val iriString = fromUri(iri)
    "<" + NTriplesParser.toURI(iriString) + ">"
  }

  def literalAsN3(literal: Rdf#Literal): String = foldLiteral(literal)(
    typedLiteral => typedLiteralAsN3(typedLiteral),
    langLiteral => {
      val (lexicalForm, lang) = fromLangLiteral(langLiteral)
      val langString = fromLang(lang)
      "\"%s\"@%s" format (NTriplesParser.toAsciiLiteral(lexicalForm), langString)
    }
  )

  def typedLiteralAsN3(typedLiteral: Rdf#TypedLiteral): String = typedLiteral match {
    case TypedLiteral(lexicalForm, datatype) if datatype == xsd.string =>
      "\"%s\"" format NTriplesParser.toAsciiLiteral(lexicalForm)
    case TypedLiteral(lexicalForm, URI(iri)) =>
      "\"%s\"^^<%s>" format (NTriplesParser.toAsciiLiteral(lexicalForm), NTriplesParser.toURI(iri))
  }

}

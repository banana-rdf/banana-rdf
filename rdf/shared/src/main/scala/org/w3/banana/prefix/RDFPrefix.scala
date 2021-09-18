package org.w3.banana.prefix

import org.w3.banana.{RDF, Ops, PrefixBuilder}

object RDFPrefix {
	def apply[Rdf <: RDF](using Ops[Rdf]) = new RDFPrefix()
}

class RDFPrefix[Rdf <: RDF](using Ops[Rdf])
	extends PrefixBuilder[Rdf](
		"rdf",
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#"):
	val langString = apply("langString") //todo: does not exist in ontology
	val nil = apply("nil")
	val typ = apply("type")
	val Alt = apply("Alt")
	val Bag = apply("Bag")
	val List = apply("List")
	val PlainLiteral = apply("PlainLiteral")
	val Property = apply("Property")
	val Seq = apply("Seq")
	val Statement = apply("Statement")
	val XMLLiteral = apply("XMLLiteral")
	val first = apply("first")
	val langRange = apply("langRange")
	val obj = apply("object")
	val predicate = apply("predicate")
	val rest = apply("rest")
	val subject = apply("subject")
	val `type` = apply("type")
	val value = apply("value")
end RDFPrefix

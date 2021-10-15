package org.w3.banana.prefix

import org.w3.banana.{RDF, Ops, PrefixBuilder}

object RDFS {
	def apply[T <: RDF](using Ops[T]) = new RDFS()
}

class RDFS[Rdf <: RDF](using Ops[Rdf])
	extends PrefixBuilder[Rdf](
		"rdfs",
		"http://www.w3.org/2000/01/rdf-schema#"):
	val Class = apply("Class")
	val Container = apply("Container")
	val ContainerMembershipProperty = apply("ContainerMembershipProperty")
	val Datatype = apply("Datatype")
	val Literal = apply("Literal")
	val Resource = apply("Resource")
	val comment = apply("comment")
	val domain = apply("domain")
	val isDefinedBy = apply("isDefinedBy")
	val label = apply("label")
	val member = apply("member")
	val range = apply("range")
	val seeAlso = apply("seeAlso")
	val subClassOf = apply("subClassOf")
	val subPropertyOf = apply("subPropertyOf")
end RDFS

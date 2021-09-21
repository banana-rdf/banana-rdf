package org.w3.banana.prefix

import org.w3.banana.{Ops, PrefixBuilder, RDF}

object WebACL:
	def apply[R <: RDF](using Ops[R]) = new WebACL[R]()


class WebACL[R <: RDF](using Ops[R])
	extends PrefixBuilder[R](
		"acl",
		"http://www.w3.org/ns/auth/acl#"
	) :
	val Authorization = apply("Authorization")
	val agent = apply("agent")
	val agentClass = apply("agentClass")
	val accessTo = apply("accessTo")
	val accessToClass = apply("accessToClass")
	val defaultForNew = apply("defaultForNew")
	val mode = apply("mode")
	val Access = apply("Access")
	val Read = apply("Read")
	val Write = apply("Write")
	val Append = apply("Append")
	val accessControl = apply("accessControl")
	val Control = apply("Control")
	val owner = apply("owner")
	val WebIDAgent = apply("WebIDAgent")

	//not officially supported:
	val include = apply("import")
end WebACL

package org.w3.banana


object Cert {
	def apply[T <: RDF](using Ops[T]) = new Cert()
}

class Cert[T <: RDF](using Ops[T])
	extends PrefixBuilder[T](
		"cert",
		"http://www.w3.org/ns/auth/cert#"
	) :
	val key = apply("key")
	val RSAKey = apply("RSAKey")
	val RSAPublicKey = apply("RSAPublicKey")
	val exponent = apply("exponent")
	val modulus = apply("modulus")
end Cert


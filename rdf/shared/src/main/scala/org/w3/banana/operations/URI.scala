package org.w3.banana.operations

import scala.util.Try
import org.w3.banana.RDF

object URI:
	val xsdStr: String = "http://www.w3.org/2001/XMLSchema#string"
	val xsdLangStr: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"

trait URI[Rdf<:RDF]:
	/** (can) throw an exception (depending on implementation of URI)
	 * different implementations decide to parse at different points, and do
	 * varying quality jobs at that (check).
	 * Need to look at how capability based exceptions could help
	 * https://github.com/lampepfl/dotty/pull/11721/files */
	def apply(uriStr: String): RDF.URI[Rdf] = mkUri(uriStr).get
	def mkUri(iriStr: String): Try[RDF.URI[Rdf]]
	protected def asString(uri: RDF.URI[Rdf]): String
	extension (uri: RDF.URI[Rdf])
		def value: String = asString(uri)
		def ===(other: RDF.URI[Rdf]): Boolean = uri.equals(other)
end URI


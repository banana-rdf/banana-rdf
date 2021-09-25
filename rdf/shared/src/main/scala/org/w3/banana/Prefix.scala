package org.w3.banana

import org.w3.banana.RDF
import org.w3.banana.RDF.*
import org.w3.banana.syntax.*

import scala.util.*

trait Prefix[Rdf <: RDF](using Ops[Rdf]):
	def prefixName: String
	def prefixIri: String
	def apply(value: String): URI[Rdf]
	def unapply(iri: URI[Rdf]): Option[String]
end Prefix

object Prefix:
	def apply[Rdf <: RDF](
		prefixName: String,
		prefixIri: String
	)(using Ops[Rdf]): Prefix[Rdf] =
		new PrefixBuilder[Rdf](prefixName, prefixIri)
end Prefix

open class PrefixBuilder[Rdf <: RDF](
	val prefixName: String,
	val prefixIri: String
)(using ops: Ops[Rdf]) extends Prefix[Rdf]:
	import ops.given
	override def toString: String = "Prefix(" + prefixName + ")"

	def apply(value: String): URI[Rdf] = ops.URI(prefixIri + value)

	def unapply(iri: URI[Rdf]): Option[String] =
		val uriString: String = iri.string
		if uriString.startsWith(prefixIri) then
			Some(uriString.substring(prefixIri.length).nn)
		else
			None

	def getLocalName(iri: URI[Rdf]): Try[String] = unapply(iri) match
		case Some(localname) => Success(localname)
		case _: None.type => Failure(Exception(this.toString + " couldn't extract localname for " + iri))

end PrefixBuilder

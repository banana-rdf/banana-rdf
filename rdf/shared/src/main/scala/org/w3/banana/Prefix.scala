package org.w3.banana

import org.w3.banana.RDF
import scala.util.*

trait Prefix[Rdf <: RDF](using val rdf: Rdf) {
  def prefixName: String
  def prefixIri: String
  def apply(value: String): rdf.URI
  def unapply(iri: rdf.URI): Option[String]
}

object Prefix {
  def apply[Rdf <: RDF](
    prefixName: String,
    prefixIri: String
  )(using rdf: Rdf): Prefix[Rdf] =
    new PrefixBuilder[Rdf](prefixName, prefixIri)
}

class PrefixBuilder[Rdf <: RDF](
  val prefixName: String,
  val prefixIri: String
)(using override val rdf: Rdf) extends Prefix[Rdf] {
  import rdf.*

  override def toString: String = "Prefix(" + prefixName + ")"

  def apply(value: String): rdf.URI = URI(prefixIri + value)

  def unapply(iri: rdf.URI): Option[String] = {
    val uriString: String = iri.asString
    if uriString.startsWith(prefixIri) then
      Some(uriString.substring(prefixIri.length).nn)
    else
      None
  }

  def getLocalName(iri: rdf.URI): Try[String] =
    unapply(iri) match {
      case Some(localname) => Success(localname)
      case _: None.type => Failure(Exception(this.toString + " couldn't extract localname for " + iri))
    }

}

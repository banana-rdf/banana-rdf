package org.w3.banana

import scalaz.NonEmptyList

/* some well-known mime-types so that we can refer to them in banana-rdf */

trait RDFSerializationFormat

trait SparqlQuery
trait N3 extends RDFSerializationFormat
trait Turtle extends RDFSerializationFormat
trait RDFXML extends RDFSerializationFormat
trait RDFaXHTML
trait SparqlAnswerJson
trait SparqlAnswerXml

sealed trait JSONLD extends RDFSerializationFormat
trait JSONLD_COMPACTED extends JSONLD
trait JSONLD_EXPANDED extends JSONLD
trait JSONLD_FLATTENED extends JSONLD

/**
 * typeclass for a Syntax
 * It must say the mime-types that are associated to it
 */
trait Syntax[+T] {

  /**
   * the mime-types for this syntax
   *
   * Per convention, the first one is the default one
   */
  def mimeTypes: NonEmptyList[String]

  /**
   * The default mime type to use for this syntax. Usually published at the IETF in their
   * <a href="http://www.iana.org/assignments/media-types/index.html">mime type registry</a>.
   */
  lazy val mime = mimeTypes.head
}

/**
 * some Syntax instances for the well-known mime-types
 */
object Syntax {

  def apply[T](implicit syntax: Syntax[T]): Syntax[T] = syntax

  implicit val RDFQueryLang: Syntax[SparqlQuery] = new Syntax[SparqlQuery] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("application/sparql-query")
  }

  implicit val N3: Syntax[N3] = new Syntax[N3] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("text/n3", "text/rdf+n3")
  }

  implicit val Turtle: Syntax[Turtle] = new Syntax[Turtle] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("text/turtle")
  }

  implicit val RDFXML: Syntax[RDFXML] = new Syntax[RDFXML] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("application/rdf+xml")
  }

  implicit val JSONLD_COMPACTED: Syntax[JSONLD_COMPACTED] = new Syntax[JSONLD_COMPACTED] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("""application/ld+json;profile="http://www.w3.org/ns/json-ld#compacted"""")
  }

  implicit val JSONLD_EXPANDED: Syntax[JSONLD_EXPANDED] = new Syntax[JSONLD_EXPANDED] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("""application/ld+json;profile="http://www.w3.org/ns/json-ld#expanded"""")
  }

  implicit val JSONLD_FLATTENED: Syntax[JSONLD_FLATTENED] = new Syntax[JSONLD_FLATTENED] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("""application/ld+json;profile="http://www.w3.org/ns/json-ld#flattened"""")
  }

  implicit val RDFaXHTML: Syntax[RDFaXHTML] = new Syntax[RDFaXHTML] {
    val mimeTypes: NonEmptyList[String] = NonEmptyList("text/html", "application/xhtml+xml")
  }

  implicit val SparqlAnswerJson = new Syntax[SparqlAnswerJson] {
    val mimeTypes = NonEmptyList("application/sparql-results+json")
  }

  implicit val SparqlAnswerXml = new Syntax[SparqlAnswerXml] {
    val mimeTypes = NonEmptyList("application/sparql-results+xml")
  }

  implicit val textPlain = new Syntax[String] {
    val mimeTypes = NonEmptyList("text/plain")
  }

}

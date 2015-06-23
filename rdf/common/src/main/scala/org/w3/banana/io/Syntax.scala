package org.w3.banana
package io

import scalaz.NonEmptyList

/* Some well-known syntaxes so that we can refer to them in banana-rdf. */

trait N3
trait Turtle
trait NTriples
trait RDFXML
trait RDFaXHTML
trait SparqlAnswerJson
trait SparqlAnswerXml

trait JsonLd
trait JsonLdCompacted
trait JsonLdExpanded
trait JsonLdFlattened

/** A syntax is defined by the Mime-Types associated with it. */
trait Syntax[+T] {

  /** The mime-types for this syntax.
    *
    * Per convention, the first one is the default one.
    */
  def mimeTypes: NonEmptyList[MimeType]

}


object Syntax {

  /** Syntax enhancements for [[Syntax]]. */
  implicit class SyntaxW[T](val syntaxMime: Syntax[T]) extends AnyVal {

    /** The default mime type to use for this syntax. Usually published at the IETF in their
      * <a href="http://www.iana.org/assignments/media-types/index.html">mime type registry</a>.
      */
    def defaultMimeType: MimeType = syntaxMime.mimeTypes.head

  }

  def apply[T](implicit syntax: Syntax[T]): Syntax[T] = syntax

  /* some [[Syntax]]es for the well-known mime-types. */

  implicit val RDFQueryLang: Syntax[RDF#Query] = new Syntax[RDF#Query] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType.SparqlQuery)
  }

  implicit val N3: Syntax[N3] = new Syntax[N3] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType("text", "n3"), MimeType("text", "rdf+n3"))
  }

  implicit val Turtle: Syntax[Turtle] = new Syntax[Turtle] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType.RdfTurtle)
  }

  implicit val NTriples: Syntax[NTriples] = new Syntax[NTriples] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType.NTriples)
  }


  implicit val RDFXML: Syntax[RDFXML] = new Syntax[RDFXML] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType.RdfXml)
  }

  implicit val JsonLdCompacted: Syntax[JsonLdCompacted] = new Syntax[JsonLdCompacted] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(
      MimeType("application", "ld+json", Map("profile" -> "http://www.w3.org/ns/json-ld#compacted"))
    )
  }

  implicit val JsonLdExpanded: Syntax[JsonLdExpanded] = new Syntax[JsonLdExpanded] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(
      MimeType("application", "ld+json", Map("profile" -> "http://www.w3.org/ns/json-ld#expanded"))
    )
  }

  implicit val JsonLdFlattened: Syntax[JsonLdFlattened] = new Syntax[JsonLdFlattened] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(
      MimeType("application", "ld+json", Map("profile" -> "http://www.w3.org/ns/json-ld#flattened"))
    )
  }

  implicit val JsonLd: Syntax[JsonLd] = new Syntax[JsonLd] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType.JsonLD)
  }

  implicit val RDFaXHTML: Syntax[RDFaXHTML] = new Syntax[RDFaXHTML] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType("text", "html"), MimeType("application", "xhtml+xml"))
  }

  implicit val SparqlAnswerJson = new Syntax[SparqlAnswerJson] {
    val mimeTypes = NonEmptyList(MimeType("application", "sparql-results+json"))
  }

  implicit val SparqlAnswerXml = new Syntax[SparqlAnswerXml] {
    val mimeTypes = NonEmptyList(MimeType("application", "sparql-results+xml"))
  }

  implicit val SparqlUpdate = new Syntax[RDF#UpdateQuery] {
    val mimeTypes = NonEmptyList(MimeType("application", "sparql-update"))
  }

  implicit val textPlain = new Syntax[String] {
    val mimeTypes = NonEmptyList(MimeType("text", "plain"))
  }

}

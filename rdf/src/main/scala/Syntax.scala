package org.w3.banana

/* some well-known mime-types so that we can refer to them in banana-rdf */

trait SparqlQuery
trait N3
trait Turtle
trait RDFXML
trait RDFaXHTML
trait SparqlAnswerJson
trait SparqlAnswerXML


/**
 * typeclass for a Syntax
 * It must say the mime-types that are associated to it
 */
trait Syntax[T] {

  /**
   * the mime-types for this syntax
   *
   * Per convention, the first one is the default one
   */
  def mimeTypes: List[MimeType]

}

/**
 * some Syntax instances for the well-known mime-types
 */
object Syntax {

  implicit val RDFQueryLang: Syntax[SparqlQuery] = new Syntax[SparqlQuery] {
    val mimeTypes: List[MimeType] = List(MimeType("application/sparql-query"))
  }

  implicit val N3: Syntax[N3] = new Syntax[N3] {
    val mimeTypes: List[MimeType] = List(MimeType("text/n3"), MimeType("text/rdf+n3"))
  }

  implicit val Turtle: Syntax[Turtle] = new Syntax[Turtle] {
    val mimeTypes: List[MimeType] = List(MimeType("text/turtle"))
  }

  implicit val RDFXML: Syntax[RDFXML] = new Syntax[RDFXML] {
    val mimeTypes: List[MimeType] = List(MimeType("application/rdf+xml"))
  }

  implicit val RDFaXHTML: Syntax[RDFaXHTML] = new Syntax[RDFaXHTML] {
    val mimeTypes: List[MimeType] = List(MimeType("text/html"), MimeType("application/xhtml+xml"))
  }

  implicit val SparqlAnswerJson = new Syntax[SparqlAnswerJson] {
    def mimeTypes = List(MimeType("application/sparql-results+json"))
  }

  implicit val SparqlAnswerXML = new Syntax[SparqlAnswerXML] {
    def mimeTypes = List(MimeType("application/sparql-results+xml"))
  }

}

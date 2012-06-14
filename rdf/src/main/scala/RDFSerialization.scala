package org.w3.banana


trait Language  {
  /**
   * clean a mime header.
   * This is a  separate function, as many http libraries calculate it for the user
   * @param mimeValue the value in an HTTP Accept or Content-Type header
   * @return the normalised mime string
   */
  def extract(mimeValue: String) = normalize(mimeValue.split(";")(0))

  /**
   * given a mime header this normalises it  to lower case and removes all extra white spaces
   * @param mime a plain mime strong, eg: text/html
   * @return normalised (lowercased and without white spaces) version of the mime
   */
  def normalize(mime: String) = mime.trim.toLowerCase

  /**
   * Find the language for this mime type
   *
   * @param mime an extracted normalised mime type
   */
  def fromMime(mime: String): Option[Language]

}

sealed trait RDFQueryLang extends Language {
  def fromMime(mime: String) = {
    mime match {
      case "application/sparql-query" => Some(SparqL)
      case _ => None
    }
  }
}

object RDFQueryLang extends RDFQueryLang

trait SparqL extends RDFQueryLang
case object SparqL extends SparqL

sealed trait RDFSerialization extends Language {
  def fromMime(mime: String): Option[RDFSerialization] = mime match {
    case "text/n3" => Some(N3)
    case "text/rdf+n3"=>Some(N3)
    case "text/turtle" => Some(Turtle)
    case "application/rdf+xml" => Some(RDFXML)
    case "text/html" => Some(RDFaHTML)
    case "application/xhtml+xml" => Some(RDFaXHTML)
    case _ => None
  }
}
object RDFSerialization extends RDFSerialization

trait RDFXML extends RDFSerialization
case object RDFXML extends RDFXML

trait RDFXMLAbbrev extends RDFXML
case object RDFXMLAbbrev extends RDFXMLAbbrev


trait N3 extends RDFSerialization
case object N3 extends N3

trait Turtle extends N3
case object Turtle extends Turtle

trait RDFa extends RDFSerialization
case object RDFa extends RDFa

trait RDFaXHTML extends RDFSerialization
case object RDFaXHTML extends RDFa

trait RDFaHTML extends RDFSerialization
case object RDFaHTML extends RDFa

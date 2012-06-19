package org.w3.banana

import collection.immutable.HashSet

/**
 * Sets of Languages grouped in some way
 */

abstract class Language(val mime: String)

case class Languages(langs: Set[Language]) {
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

  lazy val mimeLangMap: Map[String,Language] = langs.map(lang=>(lang.mime,lang)).toMap

  /**
   * Find the language for this mime type
   *
   * @param mime an extracted normalised mime type
   */
  def fromMime(mime: String): Option[Language] = mimeLangMap.get(mime)
}

// One would nearly like this set to grow so that whenever a new language is created it gets added to the set
object AllLanguages extends Languages(RDFSerialisation.langs ++ RDFQueryLanguages.langs ++ AnswerLanguages.langs )

object RDFSerialisation extends Languages(HashSet(N3,Turtle,RDFXML,RDFaHTML,RDFaXHTML))

object RDFQueryLanguages extends Languages(HashSet(SparqlQuery))

object AnswerLanguages extends Languages(HashSet(SparqlAnswerJson,SparqlAnswerXML))

case object SparqlAnswerJson extends Language("application/sparql-results+json")
case object SparqlAnswerXML extends Language("application/sparql-results+xml")

case object SparqlQuery extends Language("application/sparql-query")

//todo, deal with serialisations with multiple mime types
//case object RDFSerialization extends Languages(N3,Turtle,RDFXML,RDFaHTML,RDFaXHTML)
//  def fromMime(mime: String): Option[RDFSerialization] = mime match {
//    case "text/n3" => Some(N3)
//    case "text/rdf+n3"=>Some(N3)
//    case "text/turtle" => Some(Turtle)
//    case "application/rdf+xml" => Some(RDFXML)
//    case "text/html" => Some(RDFaHTML)
//    case "application/xhtml+xml" => Some(RDFaXHTML)
//    case _ => None
//  }
//}
//object RDFSerialization extends RDFSerialization

class RDFXML extends Language("application/rdf+xml")
case object RDFXML extends RDFXML
case object RDFXMLAbbrev extends RDFXML


class N3(lang: String) extends Language(lang)
case object N3 extends N3("text/n3")

class Turtle(mime: String) extends N3(mime)
case object Turtle extends Turtle("text/turtle")

case object RDFa extends Language("text/html")
case object RDFaXHTML extends Language("application/xhtml+xml")
case object RDFaHTML extends Language("text/html")

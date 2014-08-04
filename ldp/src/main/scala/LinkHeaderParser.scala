package org.w3.banana.ldp


import org.w3.banana.{DCTPrefix, DCPrefix, RDFOps, RDF}
import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parser for HTTP Link headers as defined by RFC5988 http://tools.ietf.org/html/rfc5988#section-5
 * @param ops
 * @tparam Rdf
 */
class LinkHeaderParser[Rdf<:RDF](implicit ops: RDFOps[Rdf]) extends JavaTokenParsers  {

  import ops._

  val dc =  DCPrefix[Rdf]
  val dct =  DCTPrefix[Rdf]

  case class TripleBuilder(rel: Rdf#URI, obj: Rdf#Node, inv: Boolean = false, subject:Option[Rdf#Node]=None) {
    def toTriple(anchor: Rdf#URI) = {
      if (inv) Triple(obj,rel,subject.getOrElse(anchor))
      else Triple(subject.getOrElse(anchor),rel,obj)
    }
  }

  trait Param
  case class Rel(uri: Rdf#URI, rev: Boolean) extends Param
  object Rel {

    def toUri(s: String) = {
      if (s.contains(':')) URI(s)
      else URI("http://www.iana.org/assignments/link-relations/#"+s) // the URIs have to be made up here, as IANA has not RDFized this
    }

    def apply(u: String, rev: Boolean): Rel = Rel(toUri(u),rev)
  }

  case class Anchor(uri: Rdf#URI) extends Param
  case class Title(s: Rdf#Literal) extends Param
  case class HrefLang(lang: Rdf#Literal) extends Param

  lazy val links: Parser[List[Rdf#Triple]] = rep1sep(link, ",").map{_.flatten}

  lazy val link: Parser[List[Rdf#Triple]] = ("<"~>uri<~">")~rep1(";"~> param) ^^ {
    case uriStr ~ attVals => {
      val obj = URI(uriStr)
      var anchor=URI("")
      val trpls = for (av <- attVals.flatten) yield {
        av match {
          case Rel(rel,rev) =>      TripleBuilder(rel,obj,rev)
          case Anchor(a)=> { anchor = a ; null }
          case HrefLang(lang)=> TripleBuilder(dc.language,lang,subject=Some(obj))
          case Title(title)=>   TripleBuilder(dct.title,title,subject=Some(obj))
          case _ => null
        }
      }
      trpls.withFilter(_!=null).map(_.toTriple(anchor))
    }
  }

  lazy val uri = """[^>]*""".r

  lazy val param: Parser[List[Param]] = (("rel"~>"="~>relation_types)^^{strs => strs.map(Rel(_,false))}) |
    ("anchor"~>"="~>"\""~>uriRef<~"\"")^^{ref=>List(Anchor(URI(ref)))} |
    ("rev"~>"="~>relation_types)^^{strs => strs.map(Rel(_,true))} |
    ("title"~>"="~>quoted_string)^^{str=>List(Title(TypedLiteral(str)))} |
    ("title*"~>"="~>ext_value)^^{lit=>List(Title(lit))} |
    ("hreflang"~>"="~>language)^^{l=>List(HrefLang(TypedLiteral(l)))}

  val paramName = """[\w!#$%&+\-^_`{}~]+""".r

  lazy val relation_types: Parser[List[String]] = relation_type^^{List(_)} | ("\""~>rep1(relation_type)<~"\"")
  lazy val relation_type = """[^;\s"]+""".r  // we will parse the URI later, no need to write a parser here
  lazy val quoted_string: Parser[String] = "\""~>rep(qdtext|qdpair)<~"\""^^{ls=>
      val ls2 = ls.map(s=> if (s.length==2 && s.charAt(0)=='"') s.substring(1) else s )
      ls2.mkString
    }

  lazy val uriRef = """[^"]*""".r //very simplified, but should be good enough

  lazy val qdtext = """[^"]*""".r
  lazy val qdpair = """\\\p{ASCII}""".r
  lazy val ext_value: Parser[Rdf#Literal] = (charset~>("'"~>opt(language)<~"'")~valueChars)^^{
    case Some(lang)~str => makeLangLiteral(str,makeLang(lang)) //todo: verify that this is the same sequence RDF uses
    case None~str => TypedLiteral(str).asInstanceOf[Rdf#Literal]
  }
  lazy val charset = "UTF-8" | "ISO-8859-1" //| mime_charset (the mime_charsets are reserved for future use )
  lazy val mime_charset = """[\p{Alnum}!#$%&+\-^_`{}]+""".r
  lazy val valueChars = rep(pctEncoded^^{pe=>
    new String(pe.split('%').drop(1).map(Integer.parseInt(_,16).toByte),"UTF-8")
  }|attrChar)^^{_.mkString}
  lazy val attrChar = """[\p{Alnum}!#$&+\-.^_`|~]+""".r
  lazy val pctEncoded = """(%\p{XDigit}\p{XDigit})+""".r
  lazy val language = """\p{Alpha}\p{Alpha}\p{Alpha}?[\-\p{Alnum}/]*""".r   //not as precisely specified in the RFC, but this will do

  //  lazy val reg_rel_type = """[a-z][a-z0-9.\-]*""".r
  //  lazy val ext_rel_type: Parser[String] = scheme~":"~hier_part~opt( "?"~query )~ opt( "#" fragment )
  //  lazy val scheme = """\p{Alpha}[\p{Alnum}+\-.]*""".r
  //  lazy val hier_part = """//"""
  //  lazy val query =
  //  lazy val fragment =
  def parse(input: String): Rdf#Graph = {
    val triples = parseAll(links, input).getOrElse(Nil)
    Graph(triples.toIterable)
  }
}

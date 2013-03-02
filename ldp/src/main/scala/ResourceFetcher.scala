package org.w3.banana.ldp

import java.net.URL
import org.w3.banana._
import concurrent.{ExecutionContext, Future}
import org.slf4j.LoggerFactory
import play.api.libs.ws.WS
import com.ning.http.client.FluentCaseInsensitiveStringsMap
import collection.JavaConverters._
import util.Success
import util.Failure
import play.api.libs.ws.ResponseHeaders
import scala.Some
import play.core.utils.CaseInsensitiveOrdered
import collection.immutable.TreeMap
import util.parsing.combinator.JavaTokenParsers


trait ResourceFetcher[Rdf<:RDF] {
  def fetch(url: URL): Future[NamedResource[Rdf]]
}

object PlantainResourceFetcher {
  val log = LoggerFactory.getLogger(this.getClass)
}


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
        System.out.println(av)
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



/**
* This Fetcher uses Play's WS
*
* @param graphSelector
* @tparam Rdf
*/
class WSFetcher[Rdf<:RDF](graphSelector: ReaderSelector[Rdf])
                         (implicit ops: RDFOps[Rdf], ec: ExecutionContext) extends ResourceFetcher[Rdf] {
  import PlantainResourceFetcher.log
  import ops._
  import syntax.graphW

  val parser = new LinkHeaderParser[Rdf]

  /**
   * following RFC5988 http://tools.ietf.org/html/rfc5988
   * todo: map all the other headers to RDF graphs where it makes sense
   */
  def parseLinkHeaders(base: Rdf#URI, linkHeaders: List[String]): PointedGraph[Rdf] =
    PointedGraph(base,union(linkHeaders.map(parser.parse(_).resolveAgainst(base))))


  def fetch(url: URL): Future[NamedResource[Rdf]] = {
    PlantainResourceFetcher.log.info(s"WebProxy: fetching $url")
    /**
     * note we prefer rdf/xml and turtle over html, as html does not always contain rdfa, and we prefer those over n3,
     * as we don't have a full n3 parser. Better would be to have a list of available parsers for whatever rdf framework is
     * installed (some claim to do n3 when they only really do turtle)
     * we can't currently accept as we don't have GRDDL implemented
     */
    //todo, add binary support
    //todo: deal with redirects...
    val response = WS.url(url.toString)
      .withHeaders("Accept" -> "application/rdf+xml,text/turtle,application/xhtml+xml;q=0.8,text/html;q=0.7,text/n3;q=0.2")
      .get
    response.flatMap {
      response =>
        import MimeType._
        log.info(s"Web Proxy fetched content successfully. ${response}")
        response.header("Content-Type") match {
          case Some(header) => {
            val mt = MimeType(extract(header))
            val gs = graphSelector(mt)
            gs match {
              case Some(r) => r.read(response.body, url.toString) match {
                //todo: add base & use binary type
                case Success(g) => {
//                 val l = parseLinkHeaders(response.ahcResponse.getHeaders.get("Link"))
                 Future.successful(LocalLDPR(URI(url.toString), g, None))
                }
                case Failure(e) => Future.failed(WrappedException("had problems parsing document returned by server", e))
              }
              case None => {
                Future.failed(LocalException("no Iteratee/parser for Content-Type " + response.header("Content-Type")))
              }
            }
          }
          case None => Future.failed(RemoteException.netty("no Content-Type header specified in response returned by server ", response.status, response.getAHCResponse.getHeaders))
        }
    }
  }

}


case class GraphNHeaders[Rdf<:RDF](graph: Rdf#Graph, remote: ResponseHeaders)

trait FetchException extends BananaException
case class RemoteException(msg: String, remote: ResponseHeaders) extends FetchException
object RemoteException {
  def netty(msg: String, code: Int, headers: FluentCaseInsensitiveStringsMap) = {
    //todo: move this somewhere else
    val res = mapAsScalaMapConverter(headers).asScala.map(e => e._1 -> e._2.asScala.toSeq).toMap
    RemoteException(msg,ResponseHeaders(code, TreeMap(res.toSeq: _*)(CaseInsensitiveOrdered)))
  }
}
case class LocalException(msg: String) extends FetchException
case class WrappedException(msg: String, e: Throwable) extends FetchException
case class WrongTypeException(msg: String) extends FetchException

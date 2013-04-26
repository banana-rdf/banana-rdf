package org.w3.banana.ldp

import com.ning.http.client.FluentCaseInsensitiveStringsMap
import com.ning.http.util.DateUtil
import concurrent.{ExecutionContext, Future}
import java.net.URL
import org.slf4j.LoggerFactory
import org.w3.banana._
import org.w3.play.api.libs.ws.{Response, ResponseHeaders, WS}
import scala.Some
import util.{Try, Success, Failure}
import java.util.concurrent.atomic.AtomicReference
import scala.collection.immutable

/**
 * A Web Client interacts directly with http resources on the web.
 * It has a higher level API to deal with the types of requests an LDP server wants to do, in order to
 * make it easy to replace different implementations ( such as a testing api ).
 *
 * @tparam Rdf
 */
trait WebClient[Rdf<:RDF] {

  def get(url: Rdf#URI): Future[NamedResource[Rdf]]
  def post[S](url: Rdf#URI, slug: Option[String], graph: Rdf#Graph,syntax: Syntax[S])
             (implicit writer: Writer[Rdf#Graph,S]): Future[Rdf#URI]
  def delete(url: Rdf#URI): Future[Unit]
  def patch(uri: Rdf#URI, remove: Iterable[TripleMatch[Rdf]], add: Iterable[Rdf#Triple]): Future[Void]  = ???
}

object WebClient {
  val log = LoggerFactory.getLogger(this.getClass)
}




/**
* This WebClient uses Play's WS
*
* @param graphSelector
* @tparam Rdf
*/
class WSClient[Rdf<:RDF](graphSelector: ReaderSelector[Rdf], rdfWriter: RDFWriter[Rdf,Turtle])
                         (implicit ops: RDFOps[Rdf], ec: ExecutionContext) extends WebClient[Rdf] {

  import ops._
  import syntax.graphW
  import syntax.URISyntax._

  val parser = new LinkHeaderParser[Rdf]

  /**
   * following RFC5988 http://tools.ietf.org/html/rfc5988
   * todo: map all the other headers to RDF graphs where it makes sense
   */
  def parseHeaders(base: Rdf#URI, headers: FluentCaseInsensitiveStringsMap): PointedGraph[Rdf] = {
    import collection.convert.decorateAsScala._
    val linkHeaders = headers.get("Link").asScala.toList
    val linkgraph = union(linkHeaders.map(parser.parse(_).resolveAgainst(base)))
    PointedGraph(base, linkgraph)
  }

  //cache does not need to be strongly synchronised, as losses are permissible
  val cache = new AtomicReference(immutable.HashMap[Rdf#URI,Future[NamedResource[Rdf]]]())

  protected def fetch(url: Rdf#URI): Future[NamedResource[Rdf]] = {
    WebClient.log.info(s"WebProxy: fetching $url")
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
    response.flatMap { response =>
      import MimeType._
      WebClient.log.info(s"Web Proxy fetched content successfully. ${response}")
      response.header("Content-Type") match {
        case Some(header) => {
          val mt = MimeType(extract(header))
          val gs = graphSelector(mt)
          gs match {
            case Some(r) => r.read(response.body, url.toString) match {
              //todo: add base & use binary type
              case Success(g) => {
                val headers: FluentCaseInsensitiveStringsMap = response.ahcResponse.getHeaders
                val meta = parseHeaders(URI(url.toString), headers)
                val updated = Try {
                  DateUtil.parseDate(headers.getFirstValue("Last-Modified"))
                }
                Future.successful(RemoteLDPR(URI(url.toString), g, meta, updated.toOption))
              }
              case Failure(e) => Future.failed(WrappedException("had problems parsing document returned by server", e))
            }
            case None => {
              Future.failed(LocalException("no Iteratee/parser for Content-Type " + response.header("Content-Type")))
            }
          }
        }
        case None => Future.failed(RemoteException.netty("no Content-Type header specified in response returned by server ", response))
      }
    }
  }

  /** This caches results */
  def get(url: Rdf#URI): Future[NamedResource[Rdf]] = {
    val c = cache.get()
    c.get(url).getOrElse {
      val result = fetch(url)
      cache.set(c + (url -> result))
      result
    }
  }

  /**
   * Post a graph to the given URL which should be a collection. Graph is posted in Turtle.
   * @param url the LDPC URL
   * @param slug a name the client prefers
   * @param graph the graph to post
   * @return The Future URL of the created resource
   */
  def post[S](url: Rdf#URI, slug: Option[String], graph: Rdf#Graph, syntax: Syntax[S])
             (implicit writer: Writer[Rdf#Graph, S]): Future[Rdf#URI] = {
    val headers = ("Content-Type" -> syntax.mime) :: slug.toList.map(slug => ("Slug" -> slug))
    val futureResp = WS.url(url.toString).withHeaders(headers: _*).post(graph, syntax)
    futureResp.flatMap { resp =>
      if (resp.status == 201) {
        resp.header("Location").map {
          loc => Future.successful(URI(loc))
        } getOrElse {
          Future.failed(RemoteException.netty("No location URL", resp))
        }
      } else {
        Future.failed(RemoteException.netty("Resource creation failed", resp))
      }
    }
  }

  def delete(url: Rdf#URI): Future[Unit] = {
    val futureResp = WS.url(url.toString).delete()
    futureResp.flatMap{ resp =>
      if (resp.status == 200 || resp.status == 202 || resp.status == 204) {
        cache.set(cache.get() - url.fragmentLess)
        Future.successful(())
      } else {
        Future.failed(RemoteException.netty("resource deletion failed",resp))
      }
    }
  }

}


//case class GraphNHeaders[Rdf<:RDF](graph: Rdf#Graph, remote: ResponseHeaders)

trait FetchException extends BananaException

case class RemoteException(msg: String, remote: ResponseHeaders) extends FetchException
object RemoteException {
  def netty(msg: String, resp: Response) = {
    RemoteException(msg,ResponseHeaders(resp.status, WS.ningHeadersToMap(resp.ahcResponse.getHeaders)))
  }
}

case class LocalException(msg: String) extends FetchException
case class WrappedException(msg: String, e: Throwable) extends FetchException
case class WrongTypeException(msg: String) extends FetchException

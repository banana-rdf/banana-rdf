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


trait ResourceFetcher[Rdf<:RDF] {
  def fetch(url: URL): Future[NamedResource[Rdf]]
}

object PlantainResourceFetcher {
  val log = LoggerFactory.getLogger(this.getClass)
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

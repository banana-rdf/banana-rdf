package org.w3.banana.sesame.io

/**
 * @author jmv
 */

import java.io._
import java.util.LinkedList
import com.github.jsonldjava.sesame.SesameJSONLDParser
import org.openrdf.model._
import org.openrdf.model.impl.{ LinkedHashModel, LiteralImpl, StatementImpl }
import org.w3.banana._
import org.w3.banana.io._
import org.w3.banana.sesame.Sesame
import scala.util.Try

class SesameRDFLoader(implicit val ops: RDFOps[Sesame]) extends RDFLoader[Sesame, Try] {
   import org.openrdf.rio.Rio
   import java.net.URL
   import java.net.HttpURLConnection
   import java.net.URLConnection

  def load(url: URL): Try[Sesame#Graph] = Try {
    val connection = getConnection(url)
    val parser = getParser(url, connection)
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops: RDFOps[Sesame] = SesameRDFLoader.this.ops
    }
    parser.setRDFHandler(collector)
    val in: InputStream = connection.getInputStream
    parser.parse(in, url.toString)
    new LinkedHashModel(triples)
  }

  private def getConnection(url: URL): URLConnection = {
      val connection = url.openConnection()
      connection match {
        case connection: HttpURLConnection =>
        val acceptHeaderTurtlePriority =
            "text/turtle;q=1, " +
            "application/rdf+xml;q=0.8, " +
            "application/ld+json;q=0.7, " +
            "text/n3;q=0.6"
        connection.setRequestProperty("Accept", acceptHeaderTurtlePriority )
        connection.connect()
        case _ =>
      }
      connection
  }

  private def getParser(url: URL, connection: URLConnection): org.openrdf.rio.RDFParser =  {
    val urlString = url.toString()
    // cf http://rdf4j.org/sesame/2.7/docs/users.docbook?view#chapter-rio4
    val parserForFileName = Rio.getParserFormatForFileName(urlString)
    val format =
      connection match {
        case connection: HttpURLConnection =>
        val contentTypeFromConnection = connection.getContentType() 
//        println("contentTypeFromConnection : " + contentTypeFromConnection )
        contentTypeFromConnection match {
          case "text/plain" => parserForFileName
          case "" => parserForFileName
          case _ => Rio.getParserFormatForMIMEType( contentTypeFromConnection )
        }
        case _ => parserForFileName
    }
//    println("format: " + format)
    Rio.createParser(format)
  }
}
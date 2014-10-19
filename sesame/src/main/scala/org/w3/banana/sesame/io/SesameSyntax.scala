package org.w3.banana.sesame.io

import java.io.{ OutputStream, Writer }
import java.net.{ URI => jURI }

import com.github.jsonldjava.sesame.SesameJSONLDWriter
import org.openrdf.model.URI
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.helpers.{ JSONLDMode, JSONLDSettings }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.w3.banana.io._

/**typeclass that reflects a Jena String that can be used to construct a BlockingReader */
trait SesameSyntax[T] {
  def rdfWriter(os: OutputStream, base: String): RDFWriter
  def rdfWriter(wr: Writer, base: String): RDFWriter
}

object SesameSyntax {

  implicit val RDFXML: SesameSyntax[RDFXML] = new SesameSyntax[RDFXML] {
    def rdfWriter(os: OutputStream, base: String) = new SRdfXmlWriter(os)

    def rdfWriter(wr: Writer, base: String) = new SRdfXmlWriter(wr)
  }

  implicit val Turtle: SesameSyntax[Turtle] = new SesameSyntax[Turtle] {
    // Sesame's parser does not handle relative URI, but let us override the behavior :-)
    def write(uri: URI, writer: Writer, baseURI: jURI) = {
      val juri = new jURI(uri.toString)
      val uriToWrite = baseURI.relativize(juri)
      writer.write("<" + uriToWrite + ">")
    }

    def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
      val baseUri = new jURI(base)
      override def writeURI(uri: URI): Unit = write(uri, writer, baseUri)
    }

    def rdfWriter(wr: Writer, base: String) = new STurtleWriter(wr) {
      val baseUri = new jURI(base)
      override def writeURI(uri: URI): Unit = write(uri, writer, baseUri)
    }
  }

  implicit val jsonLdCompated: SesameSyntax[JsonLdCompacted] = jsonldSyntax(JSONLDMode.COMPACT)

  implicit val jsonLdExpanded: SesameSyntax[JsonLdExpanded] = jsonldSyntax(JSONLDMode.EXPAND)

  implicit val jsonLdFlattened: SesameSyntax[JsonLdFlattened] = jsonldSyntax(JSONLDMode.FLATTEN)

  private def jsonldSyntax[T](mode: JSONLDMode) = new SesameSyntax[T] {
    def rdfWriter(os: OutputStream, base: String) = {
      val writer = new SesameJSONLDWriter(os)
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode);
      writer
    }
    def rdfWriter(wr: Writer, base: String) = {
      val writer = new SesameJSONLDWriter(wr)
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode);
      writer
    }
  }

}

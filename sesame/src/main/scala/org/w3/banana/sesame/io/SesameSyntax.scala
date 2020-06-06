package org.w3.banana.sesame.io

import java.io.{OutputStream, Writer}
import java.net.{URI => jURI}

import com.github.jsonldjava.sesame.SesameJSONLDWriter
import org.openrdf.model.{Statement, URI => sURI}
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.helpers.{JSONLDMode, JSONLDSettings}
import org.openrdf.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}
import org.w3.banana.io._

/** Typeclass that reflects a Sesame String that can be used to construct an RDFWriter. */
trait SesameSyntax[T] {
  def rdfWriter(os: OutputStream, base: String): RDFWriter
  def rdfWriter(wr: Writer, base: String): RDFWriter
}

object SesameSyntax {

  implicit val RDFXML: SesameSyntax[RDFXML] = new SesameSyntax[RDFXML] {
    import org.w3.banana.sesame.Sesame.ops._
    // Sesame's parser does not handle relative URI, but let us override the behavior :-)
    def rdfWriter(os: OutputStream, base: String) = new SRdfXmlWriter(os) {
      val baseUri = URI(base)
      override def handleStatement(st: Statement) = {
        super.handleStatement(st.relativizeAgainst(baseUri))
      }
    }

    def rdfWriter(wr: Writer, base: String) = new SRdfXmlWriter(wr) {
      val baseUri = URI(base)
      override def handleStatement(st: Statement) = {
        super.handleStatement(st.relativizeAgainst(baseUri))
      }
    }
  }

  implicit val Turtle: SesameSyntax[Turtle] = new SesameSyntax[Turtle] {
    import org.w3.banana.sesame.Sesame.ops.makeUri
    // Sesame's parser does not handle relative URI, but let us override the behavior :-)
    def relativize(uri: sURI, baseURI: jURI): Either[sURI, String] = {
      val juri = new jURI(uri.toString)
      val relative = baseURI.relativize(juri).toString

      if (relative.length > 0) Left(makeUri(relative)) else Right(relative)
    }

    def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
      val baseUri = new jURI(base)

      override def writeURI(uri: sURI): Unit = {
        val uriToWrite = relativize(uri, baseUri)
        uriToWrite.fold(
          super.writeURI,
          s => writer.write("<" + s + ">")
        )
      }
    }

    def rdfWriter(wr: Writer, base: String) = new STurtleWriter(wr) {
      val baseUri = new jURI(base)

      override def writeURI(uri: sURI): Unit = {
        val uriToWrite = relativize(uri, baseUri)
        uriToWrite.fold(
          super.writeURI,
          s => writer.write("<" + s + ">")
        )
      }
    }
  }

  implicit val jsonLdCompacted: SesameSyntax[JsonLdCompacted] = jsonldSyntax(JSONLDMode.COMPACT)

  implicit val jsonLdExpanded: SesameSyntax[JsonLdExpanded] = jsonldSyntax(JSONLDMode.EXPAND)

  implicit val jsonLdFlattened: SesameSyntax[JsonLdFlattened] = jsonldSyntax(JSONLDMode.FLATTEN)

  private def jsonldSyntax[T](mode: JSONLDMode) = new SesameSyntax[T] {
    import org.w3.banana.sesame.Sesame.ops._

    def rdfWriter(os: OutputStream, base: String) = {
      val baseUri = URI(base)
      val writer = new SesameJSONLDWriter(os) {
        override def handleStatement(st: Statement) = {
          super.handleStatement(st.relativizeAgainst(baseUri))
        }
      }
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode);
      writer
    }
    def rdfWriter(wr: Writer, base: String) = {
      val baseUri = URI(base)
      val writer = new SesameJSONLDWriter(wr)  {
        override def handleStatement(st: Statement) = {
          super.handleStatement(st.relativizeAgainst(baseUri))
        }
      }
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode);
      writer
    }
  }

}

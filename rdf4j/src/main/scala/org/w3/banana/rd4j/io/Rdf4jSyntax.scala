package org.w3.banana.rd4j.io

import java.io.{OutputStream, Writer}
import java.net.{URI => jURI}

import org.eclipse.rdf4j.model.{IRI, Statement}
import org.w3.banana.io._
import org.eclipse.rdf4j.rio.RDFWriter
import org.eclipse.rdf4j.rio.helpers.{JSONLDMode, JSONLDSettings}
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter
import org.eclipse.rdf4j.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}
import org.eclipse.rdf4j.rio.turtle.{TurtleWriter => STurtleWriter}

/** Typeclass that reflects a Rdf4j String that can be used to construct an [[RDFWriter]]. */
trait Rdf4jSyntax[T] {
  def rdfWriter(os: OutputStream, base: String): RDFWriter
  def rdfWriter(wr: Writer, base: String): RDFWriter
}

object Rdf4jSyntax {

  implicit val RDFXML: Rdf4jSyntax[RDFXML] = new Rdf4jSyntax[RDFXML] {
    import org.w3.banana.rd4j.Rdf4j.ops._
    // Rdf4j's parser does not handle relative URI, but let us override the behavior :-)
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

  implicit val Turtle: Rdf4jSyntax[Turtle] = new Rdf4jSyntax[Turtle] {
    import org.w3.banana.rd4j.Rdf4j.ops._
    // Rdf4j's parser does not handle relative URI, but let us override the behavior :-)
    def relativize(uri: IRI, baseURI: jURI): Either[IRI, String] = {
      val juri = new jURI(uri.toString)
      val relative = baseURI.relativize(juri).toString

      if (relative.length > 0) Left(makeUri(relative)) else Right(relative)
    }

    def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
      val baseUri = new jURI(base)

      override def writeURI(uri: IRI): Unit = {
        val uriToWrite = relativize(uri, baseUri)
        uriToWrite.fold(
          super.writeURI,
          s => writer.write("<" + s + ">")
        )
      }
    }

    def rdfWriter(wr: Writer, base: String) = new STurtleWriter(wr) {
      val baseUri = new jURI(base)

      override def writeURI(uri: IRI): Unit = {
        val uriToWrite = relativize(uri, baseUri)
        uriToWrite.fold(
          super.writeURI,
          s => writer.write("<" + s + ">")
        )
      }
    }
  }

  implicit val jsonLdCompacted: Rdf4jSyntax[JsonLdCompacted] = jsonldSyntax(JSONLDMode.COMPACT)

  implicit val jsonLdExpanded: Rdf4jSyntax[JsonLdExpanded] = jsonldSyntax(JSONLDMode.EXPAND)

  implicit val jsonLdFlattened: Rdf4jSyntax[JsonLdFlattened] = jsonldSyntax(JSONLDMode.FLATTEN)

  private def jsonldSyntax[T](mode: JSONLDMode) = new Rdf4jSyntax[T] {
    import org.w3.banana.rd4j.Rdf4j.ops._

    def rdfWriter(os: OutputStream, base: String) = {
      val baseUri = URI(base)
      val writer = new JSONLDWriter(os) {
        override def handleStatement(st: Statement) = {
          super.handleStatement(st.relativizeAgainst(baseUri))
        }
      }
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode)
      writer
    }
    def rdfWriter(wr: Writer, base: String) = {
      val baseUri = URI(base)
      val writer = new JSONLDWriter(wr)  {
        override def handleStatement(st: Statement) = {
          super.handleStatement(st.relativizeAgainst(baseUri))
        }
      }
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode)
      writer
    }
  }

}


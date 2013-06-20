package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.rio.RDFWriter
import java.io.{ Writer, OutputStream }
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.model.URI

import com.github.jsonldjava.impl._
import org.openrdf.rio.helpers.{JSONLDMode, JSONLDSettings}

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
    def write(uri: URI, writer: Writer, baseURI: String) = {
      val uriString = uri.toString
      val uriToWrite =
        if (uriString startsWith baseURI)
          uriString.substring(baseURI.length)
        else
          uriString
      writer.write("<" + uriToWrite + ">")
    }

    def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
      override def writeURI(uri: URI): Unit = write(uri, writer, base)
    }

    def rdfWriter(wr: Writer, base: String) = new STurtleWriter(wr) {
      override def writeURI(uri: URI): Unit = write(uri, writer, base)
    }
  }

  implicit val JSONLD: SesameSyntax[JSONLD] = new SesameSyntax[JSONLD] {
    def rdfWriter(os: OutputStream, base: String) =
    {
      val writer = new SesameJSONLDWriter(os)
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
      writer
    }

    def rdfWriter(wr: Writer, base: String) = {
      val writer = new SesameJSONLDWriter(wr)
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
      writer
    }
  }

}

package org.w3.banana.sesame.io

import java.io.{OutputStreamWriter, OutputStream, Writer}
import java.net.{URI => jURI}
import java.nio.charset.Charset

import com.github.jsonldjava.sesame.SesameJSONLDWriter
import org.openrdf.model.{Statement, URI => sURI}
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.helpers.{JSONLDMode, JSONLDSettings}
import org.openrdf.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}
import org.w3.banana.io._

/** Typeclass that reflects a Jena String that can be used to construct an [[RDFWriter]]. */
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

    def rdfWriter(os: OutputStream, base: String) = new RelativeWriter(os,base)

    def rdfWriter(wr: Writer, base: String) = new RelativeWriter(wr,base)

    class RelativeWriter(wr:Writer,base:String) extends STurtleWriter(wr)
    {
      val baseUri = new jURI(base)
      def this(out:OutputStream,base:String) =this(new OutputStreamWriter(out, Charset.forName("UTF-8")),base)

      // Sesame's parser does not handle relative URI, but let us override the behavior :-)
      override def writeURI(uri: sURI): Unit = {
        val juri = new jURI(uri.toString)
        val uriToWrite = baseUri.relativize(juri)
        if(juri == uriToWrite) super.writeURI(uri) else writer.write("<" + uriToWrite + ">")
      }
    }
  }

  implicit val jsonLdCompacted: SesameSyntax[JsonLdCompacted] = jsonldSyntax(JSONLDMode.COMPACT)

  implicit val jsonLdExpanded: SesameSyntax[JsonLdExpanded] = jsonldSyntax(JSONLDMode.EXPAND)

  implicit val jsonLdFlattened: SesameSyntax[JsonLdFlattened] = jsonldSyntax(JSONLDMode.FLATTEN)

  def jsonldSyntax[T](mode: JSONLDMode) = new SesameSyntax[T] {
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

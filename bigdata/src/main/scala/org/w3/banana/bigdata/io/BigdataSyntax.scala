package org.w3.banana.bigdata.io


import java.io.{OutputStream, Writer}
import java.net.{URI => jURI}

import org.openrdf.model.{Statement, URI => sURI}
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.helpers.{JSONLDMode, JSONLDSettings}
import org.openrdf.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}
import org.w3.banana.io._

/** Typeclass that reflects a Bigdata String that can be used to construct an [[RDFWriter]]. */
trait BigdataSyntax[T] {
  def rdfWriter(os: OutputStream, base: String): RDFWriter
  def rdfWriter(wr: Writer, base: String): RDFWriter
}

object BigdataSyntax {

  implicit val RDFXML: BigdataSyntax[RDFXML] = new BigdataSyntax[RDFXML] {
    import org.w3.banana.bigdata.Bigdata.ops._
    // Bigdata's parser does not handle relative URI, but let us override the behavior :-)
    def rdfWriter(os: OutputStream, base: String) = new SRdfXmlWriter(os) {
      val baseUri = URI(base)
      override def handleStatement(st: Statement) = {
        super.handleStatement(st)
      }
    }

    def rdfWriter(wr: Writer, base: String) = new SRdfXmlWriter(wr) {
      val baseUri = URI(base)
      override def handleStatement(st: Statement) = {
        super.handleStatement(st)
      }
    }
  }

  implicit val Turtle: BigdataSyntax[Turtle] = new BigdataSyntax[Turtle] {
    def write(uri: sURI, writer: Writer, baseURI: jURI) = {
      //val juri = new jURI(uri.toString)
      //val uriToWrite = baseURI.relativize(juri)
      writer.write("<" + uri.stringValue() + ">")
    }

    def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
      val baseUri = new jURI(base)
      override def writeURI(uri: sURI): Unit = write(uri, writer, baseUri)
    }

    def rdfWriter(wr: Writer, base: String) = new STurtleWriter(wr) {
      val baseUri = new jURI(base)
      override def writeURI(uri: sURI): Unit = write(uri, writer, baseUri)
    }
  }

}

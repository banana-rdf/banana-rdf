package org.w3.banana.rdf4j.io

import org.eclipse.rdf4j.model.{Statement, IRI => rdf4jIRI}
import org.eclipse.rdf4j.rio.helpers.{JSONLDMode, JSONLDSettings}
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter
import org.eclipse.rdf4j.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}
import org.eclipse.rdf4j.rio.turtle.{TurtleWriter => STurtleWriter}
import org.eclipse.rdf4j.rio.{RDFWriter => RioRDFWriter}
import org.w3.banana.io._

import java.io.{OutputStream, Writer}
import java.net.{URI => jURI}

/** Typeclass that reflects an RDF4J String that can be used to construct an RDFWriter. */
trait Rdf4jSyntax[T] {
  def rdfWriter(os: OutputStream, base: Option[String]): RioRDFWriter
  def rdfWriter(wr: Writer, base: Option[String]): RioRDFWriter
}

object Rdf4jSyntax {

  implicit val RDFXML: Rdf4jSyntax[RDFXML] = new Rdf4jSyntax[RDFXML] {

    import org.w3.banana.rdf4j.Rdf4j.ops._

    // RDF4J's parser does not handle relative URI, but let us override the behavior :-)
    def rdfWriter(os: OutputStream, base: Option[String]) = base match {
      case None => new SRdfXmlWriter(os)
      case Some(baseStr) => new SRdfXmlWriter(os) {
        val baseUri = URI(baseStr)

        override def handleStatement(st: Statement) = {
          super.handleStatement(st.relativizeAgainst(baseUri))
        }
      }
    }


    def rdfWriter(wr: Writer, base: Option[String]) = base match {
      case None => new SRdfXmlWriter(wr)
      case Some(baseStr) => new SRdfXmlWriter(wr) {
        val baseUri = URI(baseStr)

        override def handleStatement(st: Statement) = {
          super.handleStatement(st.relativizeAgainst(baseUri))
        }
      }
    }
  }

  implicit val Turtle: Rdf4jSyntax[Turtle] = new Rdf4jSyntax[Turtle] {

    import org.w3.banana.rdf4j.Rdf4j.ops._

    // RDF4J's parser does not handle relative URI, but let us override the behavior :-)
    def relativize(uri: rdf4jIRI, baseURI: Option[jURI]): Either[rdf4jIRI, String] =
      baseURI match {
        case None => Left(uri)
        case Some(base) => {
          val juri     = new jURI(uri.toString)
          val relative = base.relativize(juri).toString

          if (relative.length > 0) Left(makeUri(relative)) else Right(relative)
        }
      }

    def rdfWriter(os: OutputStream, base: Option[String]) = new STurtleWriter(os) {
      val baseUriOpt = base.map(bs => new jURI(bs))

      override def writeURI(uri: rdf4jIRI): Unit = {
        val uriToWrite: Either[rdf4jIRI, String] = relativize(uri, baseUriOpt)
        uriToWrite.fold(
          super.writeURI,
          s => writer.write("<" + s + ">")
        )
      }
    }

    def rdfWriter(wr: Writer, base: Option[String]) = new STurtleWriter(wr) {
      val baseUriOpt = base.map(bs => new jURI(bs))

      override def writeURI(uri: rdf4jIRI): Unit = {
        val uriToWrite = relativize(uri, baseUriOpt)
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
    import org.w3.banana.rdf4j.Rdf4j.ops._

    def rdfWriter(os: OutputStream, base: Option[String]): JSONLDWriter = {
      val baseUri = base.map(bs => URI(bs))
      val writer  = baseUri match {
        case None => new JSONLDWriter(os)
        case Some(baseURI) => new JSONLDWriter(os) {
          override def handleStatement(st: Statement) = {
            super.handleStatement(st.relativizeAgainst(baseURI))
          }
        }
      }
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode);
      writer
    }

    def rdfWriter(wr: Writer, base: Option[String]): JSONLDWriter = {
      val baseUri = base.map(bs => URI(bs))
      val writer  = baseUri match {
        case None => new JSONLDWriter(wr)
        case Some(baseURI) => new JSONLDWriter(wr) {
          override def handleStatement(st: Statement) = {
            super.handleStatement(st.relativizeAgainst(baseURI))
          }
        }
      }
      writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, mode);
      writer
    }
  }
}

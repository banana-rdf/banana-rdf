package org.w3.banana.jsonldjs.io

import java.io.{ByteArrayOutputStream, OutputStream}

import org.w3.banana.io.{JsonLd, RDFWriter}
import org.w3.banana.jsonldjs.jsonldHelper
import org.w3.banana.{RDF, RDFOps}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.JSON

class JsonLdJsSerializer[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  ec: ExecutionContext
) extends RDFWriter[Rdf, Future, JsonLd] {


  private val charSet = "UTF-8"

  override def write(graph: Rdf#Graph, os: OutputStream, base: String): Future[Unit] = {
    jsonldHelper.fromRDF(graph, base).map {
      jsonld =>
        os.write(JSON.stringify(jsonld).getBytes(charSet))
    }
  }

  override def asString(graph: Rdf#Graph, base: String): Future[String] = {
    val outputStream = new ByteArrayOutputStream()
    write(graph, outputStream, base).map { _ =>
      outputStream.toString(charSet)
    }
  }

}

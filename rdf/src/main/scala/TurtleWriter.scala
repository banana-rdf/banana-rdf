package org.w3.banana

import java.io._
import org.w3.banana.scalaz._

trait BlockingWriter[Rdf <: RDF] {
    val ops: RDFOperations[Rdf]

    def write(graph: Rdf#Graph, os: OutputStream, base: String): Validation[BananaException, Unit]
    def write(graph: Rdf#Graph, writer: Writer, base: String): Validation[BananaException, Unit]
    def write(graph: Rdf#Graph, file: File, base: String): Validation[BananaException, Unit] = WrappedThrowable.fromTryCatch {
      val fos = new BufferedOutputStream(new FileOutputStream(file))
      write(graph, fos, base)
    }
    def asString(graph: Rdf#Graph, base: String): Validation[BananaException, String] = WrappedThrowable.fromTryCatch {
      val stringWriter = new StringWriter
      write(graph, stringWriter, base)
      stringWriter.toString
    }
}


trait TurtleWriter[Rdf <: RDF] extends BlockingWriter[Rdf]


trait RdfXmlWriter[Rdf <: RDF] extends BlockingWriter[Rdf]

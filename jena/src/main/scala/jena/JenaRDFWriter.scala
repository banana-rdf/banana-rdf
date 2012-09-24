package org.w3.banana.jena

import org.w3.banana._
import java.io.{ OutputStream, Writer => jWriter }
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * Create an RDF Writer using Jena's serialisers
 */
object JenaRDFWriter {

  import JenaGraphSyntax._

  def apply[T](implicit jenaSyntax: JenaGraphSyntax[T], _syntax: Syntax[T]): RDFWriter[Jena, T] =
    new RDFWriter[Jena, T] {

      val syntax: Syntax[T] = _syntax

      val serialization = jenaSyntax.value

      def write(graph: Jena#Graph, os: OutputStream, base: String) = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createModelForGraph(graph.jenaGraph)
        model.getWriter(serialization).write(model, os, base)
      }

      def write(graph: Jena#Graph, writer: jWriter, base: String) = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createModelForGraph(graph.jenaGraph)
        model.getWriter(serialization).write(model, writer, base)
      }

    }

  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter[Turtle]

  val writerSelector: RDFWriterSelector[Jena] =
    RDFWriterSelector[Jena, RDFXML] combineWith RDFWriterSelector[Jena, Turtle]

}

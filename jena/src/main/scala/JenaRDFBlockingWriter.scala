package org.w3.banana.jena

import org.w3.banana.{Turtle, RDFXML, WrappedThrowable, RDFBlockingWriter}
import java.io.{OutputStream, Writer}
import com.hp.hpl.jena.rdf.model.ModelFactory


/**
 * Create an Blocking RDF Writer using Jena's serialisers
 */
object JenaRDFBlockingWriter {

  import JenaGraphSyntax._

  def apply[SyntaxType](implicit jenaSyntax: JenaGraphSyntax[SyntaxType]): RDFBlockingWriter[Jena, SyntaxType] =
    new RDFBlockingWriter[Jena, SyntaxType] {

      val serialization = jenaSyntax.value

      def write(graph: Jena#Graph, os: OutputStream, base: String) = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createModelForGraph(graph)
        model.getWriter(serialization).write(model, os, base)
      }

      def write(graph: Jena#Graph, writer: Writer, base: String) = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createModelForGraph(graph)
        model.getWriter(serialization).write(model, writer, base)
      }

    }

  implicit val RDFXMLWriter: RDFBlockingWriter[Jena, RDFXML] = JenaRDFBlockingWriter[RDFXML]

  implicit val TurtleWriter: RDFBlockingWriter[Jena, Turtle] = JenaRDFBlockingWriter[Turtle]

}

package org.w3.banana.jena

import org.w3.banana._
import java.io.{ OutputStream, Writer }
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * Create an Blocking RDF Writer using Jena's serialisers
 */
object JenaRDFBlockingWriter {

  import JenaGraphSyntax._

  def apply[SyntaxType](implicit jenaSyntax: JenaGraphSyntax[SyntaxType],
    syntaxtp: Syntax[SyntaxType]): RDFBlockingWriter[Jena, SyntaxType] =
    new RDFBlockingWriter[Jena, SyntaxType] {

      def syntax[S >: SyntaxType]: Syntax[S] = syntaxtp

      val serialization = jenaSyntax.value

      def write(graph: Jena#Graph, os: OutputStream, base: String) = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createModelForGraph(graph.jenaGraph)
        model.getWriter(serialization).write(model, os, base)
      }

      def write(graph: Jena#Graph, writer: Writer, base: String) = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createModelForGraph(graph.jenaGraph)
        model.getWriter(serialization).write(model, writer, base)
      }

    }

  implicit val rdfxmlWriter: RDFBlockingWriter[Jena, RDFXML] = JenaRDFBlockingWriter[RDFXML]

  implicit val turtleWriter: RDFBlockingWriter[Jena, Turtle] = JenaRDFBlockingWriter[Turtle]

  val writerSelector: RDFWriterSelector[Jena#Graph] =
    RDFWriterSelector[Jena#Graph, RDFXML] combineWith RDFWriterSelector[Jena#Graph, Turtle]

}

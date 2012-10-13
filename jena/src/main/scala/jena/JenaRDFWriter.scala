package org.w3.banana.jena

import org.w3.banana._
import java.io.{ Writer => jWriter }
import scalax.io._
import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.util._

/**
 * Create an RDF Writer using Jena's serialisers
 */
object JenaRDFWriter {

  import JenaGraphSyntax._

  def apply[T](implicit jenaSyntax: JenaGraphSyntax[T], _syntax: Syntax[T]): RDFWriter[Jena, T] =
    new RDFWriter[Jena, T] {

      val syntax: Syntax[T] = _syntax

      val serialization = jenaSyntax.value

      def write[R <: jWriter](graph: Jena#Graph, wcr: WriteCharsResource[R], base: String): Try[Unit] =
        Try {
          wcr.acquireAndGet { writer =>
            val model = ModelFactory.createModelForGraph(graph.jenaGraph)
            model.getWriter(serialization).write(model, writer, base)
          }
        }

    }

  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter[Turtle]

  val selector: RDFWriterSelector[Jena] =
    RDFWriterSelector[Jena, RDFXML] combineWith RDFWriterSelector[Jena, Turtle]

}

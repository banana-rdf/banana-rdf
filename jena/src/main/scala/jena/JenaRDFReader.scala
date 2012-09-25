package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model.{ RDFReader => _, _ }
import scalaz.Validation

object JenaRDFReader {

  import JenaGraphSyntax._

  /**
   * builds an RDFReader for Jena knowing the Jena String that identify a BlockingReader
   * @param jenaSyntax
   * @tparam SyntaxType type of serialisation to write to. Usually a phantom type, useful for type class behavior and
   *                    for aligning writers implemented with different frameworks (eg: Jena or Sesame)
   * @return  an RDFREader
   */
  def apply[S](implicit _syntax: Syntax[S], jenaSyntax: JenaGraphSyntax[S]): RDFReader[Jena, S] =
    new RDFReader[Jena, S] {

      val syntax = _syntax

      val serialization = jenaSyntax.value

      def read(is: InputStream, base: String): BananaValidation[Jena#Graph] = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createDefaultModel()
        model.getReader(serialization).read(model, is, base)
        BareJenaGraph(model.getGraph)
      }

      def read(reader: Reader, base: String): BananaValidation[Jena#Graph] = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createDefaultModel()
        model.getReader(serialization).read(model, reader, base)
        BareJenaGraph(model.getGraph)
      }

    }

  implicit val rdfxmlReader: RDFReader[Jena, RDFXML] = JenaRDFReader[RDFXML]

  implicit val turtleReader: RDFReader[Jena, Turtle] = JenaRDFReader[Turtle]

  implicit val selector: ReaderSelector[Jena] = 
    ReaderSelector[Jena, RDFXML] combineWith ReaderSelector[Jena, Turtle]

}

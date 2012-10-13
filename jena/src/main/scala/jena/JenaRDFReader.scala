package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model.{ RDFReader => _, _ }
import scalax.io._
import scala.util._

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

      def read[R <: Reader](resource: ReadCharsResource[R], base: String): Try[Jena#Graph] = Try {
        resource acquireAndGet { reader => 
          val model = ModelFactory.createDefaultModel()
          model.getReader(serialization).read(model, reader, base)
          BareJenaGraph(model.getGraph)
        }
      }

    }

  implicit val rdfxmlReader: RDFReader[Jena, RDFXML] = JenaRDFReader[RDFXML]

  implicit val turtleReader: RDFReader[Jena, Turtle] = JenaRDFReader[Turtle]

  implicit val selector: ReaderSelector[Jena] = 
    ReaderSelector[Jena, RDFXML] combineWith ReaderSelector[Jena, Turtle]

}

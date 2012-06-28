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
  def apply[SyntaxType](implicit jenaSyntax: JenaGraphSyntax[SyntaxType]): BlockingReader[Jena#Graph, SyntaxType] =
    new RDFReader[Jena, SyntaxType] {

      val serialization = jenaSyntax.value

      def read(is: InputStream, base: String): Validation[BananaException, Jena#Graph] = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createDefaultModel()
        model.getReader(serialization).read(model, is, base)
        model.getGraph
      }
  
      def read(reader: Reader, base: String): Validation[BananaException, Jena#Graph] = WrappedThrowable.fromTryCatch {
        val model = ModelFactory.createDefaultModel()
        model.getReader(serialization).read(model, reader, base)
        model.getGraph
      }
  
    }

  implicit val RDFXMLReader: BlockingReader[Jena#Graph, RDFXML] = JenaRDFReader[RDFXML]

  implicit val TurtleReader: BlockingReader[Jena#Graph, Turtle] = JenaRDFReader[Turtle]

  implicit val ReaderSelector: ReaderSelector[Jena#Graph] =
    ReaderSelector2[Jena#Graph, RDFXML] combineWith ReaderSelector2[Jena#Graph, Turtle]

}

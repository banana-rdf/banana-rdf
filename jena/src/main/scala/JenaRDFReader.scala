package org.w3.banana.jena

import org.w3.banana._
import JenaOperations._
import java.io._
import com.hp.hpl.jena.rdf.model.{ RDFReader => _, _ }
import scalaz.Validation
import scalaz.Validation._

object JenaRDFReader {

  import JenaSyntax._

  /** builds an RDFReader for Jena knowing the Jena String that identify a Reader */
  def apply[T](implicit jenaSyntax: JenaSyntax[T]): RDFReader[Jena, T] =
    new RDFReader[Jena, T] {

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

  implicit val RDFXMLReader: RDFReader[Jena, RDFXML] = JenaRDFReader[RDFXML]

  implicit val TurtleReader: RDFReader[Jena, Turtle] = JenaRDFReader[Turtle]

  implicit val ReaderSelector: RDFReaderSelector[Jena] = RDFReaderSelector[Jena, RDFXML, Turtle]

}

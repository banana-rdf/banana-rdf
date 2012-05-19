package org.w3.banana.jena

import org.w3.banana._
import java.io._
import com.hp.hpl.jena.rdf.model.{RDFReader => _, _}

import scalaz.Validation
import scalaz.Validation._

trait JenaGenericReader {
  
  val serializationLanguage: String
  
  import JenaOperations._

  def read(is: InputStream, base: String): Validation[BananaException, Jena#Graph] = WrappedThrowable.fromTryCatch {
    val model = ModelFactory.createDefaultModel()
    model.getReader(serializationLanguage).read(model, is, base)
    model.getGraph
  }
  
  def read(reader: Reader, base: String): Validation[BananaException, Jena#Graph] = WrappedThrowable.fromTryCatch {
    val model = ModelFactory.createDefaultModel()
    model.getReader(serializationLanguage).read(model, reader, base)
    model.getGraph
  }
  
}

object JenaRDFXMLReader extends RDFReader[Jena, RDFXML] with JenaGenericReader {
  val serializationLanguage = "RDF/XML"
}

object JenaTurtleReader extends RDFReader[Jena, Turtle] with JenaGenericReader {
  val serializationLanguage = "TTL"
}

object JenaReaderFactory extends RDFReaderFactory[Jena] {
  val RDFXMLReader = JenaRDFXMLReader
  val TurtleReader = JenaTurtleReader
}

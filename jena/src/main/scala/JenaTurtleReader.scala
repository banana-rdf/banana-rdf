package org.w3.rdf.jena

import org.w3.rdf._
import java.io._
import com.hp.hpl.jena.rdf.model._

object JenaTurtleReader extends TurtleReader[Jena](JenaOperations) {
  
  import JenaOperations._
  
  def read(is: InputStream, base: String): Either[Throwable, Jena#Graph] =
    try {
      val model = ModelFactory.createDefaultModel()
      model.read(is,base,"TTL")
      Right(model.getGraph)
    } catch {
      case t => Left(t)
    }
  
  def read(reader: Reader, base: String): Either[Throwable, Jena#Graph] =
    try {
      val model = ModelFactory.createDefaultModel()
      model.read(reader,base,"TTL")
      Right(model.getGraph)
    } catch {
      case t => Left(t)
    }
  
  
}
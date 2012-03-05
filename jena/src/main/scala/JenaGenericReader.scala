package org.w3.rdf.jena

import org.w3.rdf._
import java.io._
import com.hp.hpl.jena.rdf.model._
import org.openjena.riot.SysRIOT

import scalaz.Validation
import scalaz.Validation._

trait JenaGenericReader {
  
  val serializationLanguage: String
  
  import JenaOperations._
  SysRIOT.wireIntoJena()

  def read(is: InputStream, base: String): Validation[Throwable, Jena#Graph] = fromTryCatch {
    val model = ModelFactory.createDefaultModel()
    model.getReader(serializationLanguage).read(model, is, base)
    model.getGraph
  }
  
  def read(reader: Reader, base: String): Validation[Throwable, Jena#Graph] = fromTryCatch {
    val model = ModelFactory.createDefaultModel()
    model.getReader(serializationLanguage).read(model, reader, base)
    model.getGraph
  }
  
  
}
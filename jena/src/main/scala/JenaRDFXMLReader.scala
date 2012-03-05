package org.w3.rdf.jena

import org.w3.rdf._
import java.io._
import com.hp.hpl.jena.rdf.model._
import org.openjena.riot.SysRIOT

import scalaz.Validation
import scalaz.Validation._

object JenaRDFXMLReader extends RDFXMLReader[Jena](JenaOperations) with JenaGenericReader {
  val serializationLanguage = "RDF/XML"
}

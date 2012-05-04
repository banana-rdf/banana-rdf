package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.model._
import org.openrdf.model.impl.{ GraphImpl, StatementImpl, LiteralImpl }
import java.io._
import java.util.LinkedList

import SesamePrefix._

import scalaz.Validation
import scalaz.Validation._

trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {
  override def handleStatement(st: Statement): Unit = st.getObject match {
    case o: Literal if o.getDatatype == null && o.getLanguage == null =>
      super.handleStatement(
        new StatementImpl(
          st.getSubject,
          st.getPredicate,
          new LiteralImpl(o.getLabel, xsd.string)))
    case other =>
      super.handleStatement(st)
  }
}

object SesameTurtleReader extends RDFReader[Sesame, Turtle] {
  
  import SesameOperations._
  
  def read(is: InputStream, base: String): Validation[Throwable, Sesame#Graph] = fromTryCatch {
    val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
    val triples = new java.util.LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    turtleParser.setRDFHandler(collector)
    turtleParser.parse(is, base)
    new GraphImpl(triples)
  }
  
  def read(reader: Reader, base: String): Validation[Throwable, Sesame#Graph] = fromTryCatch {
    val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
    val triples = new java.util.LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    turtleParser.setRDFHandler(collector)
    turtleParser.parse(reader, base)
    new GraphImpl(triples)
  }
  
}



object SesameRDFXMLReader extends RDFReader[Sesame, RDFXML] {
  
  import SesameOperations._
  
  def read(is: InputStream, base: String): Validation[Throwable, Sesame#Graph] = fromTryCatch {
    val parser = new org.openrdf.rio.rdfxml.RDFXMLParser
    val triples = new java.util.LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    parser.setRDFHandler(collector)
    parser.parse(is, base)
    new GraphImpl(triples)
  }
  
  def read(reader: Reader, base: String): Validation[Throwable, Sesame#Graph] = fromTryCatch {
    val parser = new org.openrdf.rio.rdfxml.RDFXMLParser
    val triples = new java.util.LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    parser.setRDFHandler(collector)
    parser.parse(reader, base)
    new GraphImpl(triples)
  }
  
}

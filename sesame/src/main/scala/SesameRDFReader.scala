package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.model.Statement
import org.openrdf.model.impl.{GraphImpl, StatementImpl, LiteralImpl}
import java.io._
import java.util.LinkedList

import SesamePrefix._

import scalaz.Validation
import scalaz.Validation._

object SesameTurtleReader extends RDFReader[Sesame, Turtle] {
  
  import SesameOperations._
  
  trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {
    override def handleStatement(st: Statement): Unit = st.getObject match {
      case o: LiteralImpl if o.getDatatype == null && o.getLanguage == null =>
        super.handleStatement(new StatementImpl(st.getSubject, st.getPredicate, new LiteralImpl(o.getLabel, xsd.string)))
      case other =>
        super.handleStatement(st)
    }
  }
  
  def read(is: InputStream, base: String): Validation[Throwable, Graph] = fromTryCatch {
    val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
    val triples = new java.util.LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    turtleParser.setRDFHandler(collector)
    turtleParser.parse(is, base)
    new GraphImpl(triples)
  }
  
  def read(reader: Reader, base: String): Validation[Throwable, Graph] = fromTryCatch {
    val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
    val triples = new java.util.LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    turtleParser.setRDFHandler(collector)
    turtleParser.parse(reader, base)
    new GraphImpl(triples)
  }
  
}

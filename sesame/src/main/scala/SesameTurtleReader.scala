package org.w3.rdf.sesame

import org.w3.rdf

import org.openrdf.model.Statement
import org.openrdf.model.impl.{GraphImpl, StatementImpl, LiteralImpl}
import java.io._
import java.util.LinkedList

import scala.collection.immutable.List

object SesameTurtleReader extends rdf.TurtleReader(SesameModule) {
  
  import SesameModule._
  
  trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {
    override def handleStatement(st: Statement): Unit = st.getObject match {
      case o: LiteralImpl if o.getDatatype == null && o.getLanguage == null =>
        super.handleStatement(new StatementImpl(st.getSubject, st.getPredicate, new LiteralImpl(o.getLabel, xsdString)))
      case other =>
        super.handleStatement(st)
    }
  }
  
  def read(is: InputStream, base: String): Either[Throwable, Graph] =
    try {
      val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
      val triples = new java.util.LinkedList[Statement]
      val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
      turtleParser.setRDFHandler(collector)
      turtleParser.parse(is, base)
      Right(new Graph(new GraphImpl(triples)))
    } catch {
      case t => Left(t)
    }
  
  def read(reader: Reader, base: String): Either[Throwable, Graph] =
    try {
      val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
      val triples = new java.util.LinkedList[Statement]
      val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
      turtleParser.setRDFHandler(collector)
      turtleParser.parse(reader, base)
      Right(new Graph(new GraphImpl(triples)))
    } catch {
      case t => Left(t)
    }
  
  
}
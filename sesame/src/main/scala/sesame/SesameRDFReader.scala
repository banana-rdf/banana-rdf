package org.w3.banana.sesame

import org.w3.banana._
import SesamePrefix._
import org.openrdf.model._
import org.openrdf.model.impl.{ GraphImpl, StatementImpl, LiteralImpl }
import java.io._
import java.util.LinkedList
import scalax.io._
import scala.util._

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

  val syntax = Syntax[Turtle]

  def read[R <: Reader](resource: ReadCharsResource[R], base: String): Try[Sesame#Graph] = Try {
    resource acquireAndGet { reader => 
      val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
      val triples = new LinkedList[Statement]
      val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
      turtleParser.setRDFHandler(collector)
      turtleParser.parse(reader, base)
      new GraphImpl(triples)
    }
  }

}

object SesameRDFXMLReader extends RDFReader[Sesame, RDFXML] {

  import SesameOperations._

  val syntax = Syntax[RDFXML]

  def read[R <: Reader](resource: ReadCharsResource[R], base: String): Try[Sesame#Graph] = Try {
    resource acquireAndGet { reader =>
      val parser = new org.openrdf.rio.rdfxml.RDFXMLParser
      val triples = new LinkedList[Statement]
      val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
      parser.setRDFHandler(collector)
      parser.parse(reader, base)
      new GraphImpl(triples)
    }
  }

}

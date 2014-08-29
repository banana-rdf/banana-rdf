package org.w3.banana.sesame

import java.io._
import java.util.LinkedList

import org.openrdf.model._
import org.openrdf.model.impl.{ LinkedHashModel, LiteralImpl, StatementImpl }
import org.w3.banana._

import scala.util._

trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {

  def ops: SesameOps

  override def handleStatement(st: Statement): Unit = st.getObject match {
    case o: Literal if o.getDatatype == null && o.getLanguage == null =>
      super.handleStatement(
        new StatementImpl(
          st.getSubject,
          st.getPredicate,
          new LiteralImpl(o.getLabel, ops.__xsdString)))
    case other =>
      super.handleStatement(st)
  }

}

class SesameTurtleReader(implicit Ops: SesameOps) extends RDFReader[Sesame, Turtle] {

  val syntax = Syntax[Turtle]

  def read(is: InputStream, base: String): Try[Sesame#Graph] = Try {
    val turtleParser = new org.openrdf.rio.turtle.TurtleParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops: SesameOps = Ops
    }
    turtleParser.setRDFHandler(collector)
    turtleParser.parse(is, base)
    new LinkedHashModel(triples)
  }

}

class SesameRDFXMLReader(implicit Ops: SesameOps) extends RDFReader[Sesame, RDFXML] {

  val syntax = Syntax[RDFXML]

  def read(is: InputStream, base: String): Try[Sesame#Graph] = Try {
    val parser = new org.openrdf.rio.rdfxml.RDFXMLParser
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops: SesameOps = Ops
    }
    parser.setRDFHandler(collector)
    parser.parse(is, base)
    new LinkedHashModel(triples)
  }

}

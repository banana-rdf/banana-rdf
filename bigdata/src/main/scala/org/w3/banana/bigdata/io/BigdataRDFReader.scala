package org.w3.banana.bigdata.io
/*

import java.io._
import java.util.LinkedList

import org.openrdf.model._
import org.openrdf.model.impl.{LinkedHashModel, LiteralImpl, StatementImpl}
import org.w3.banana._
import org.w3.banana.bigdata.Bigdata
import org.w3.banana.io._

import scala.util._

trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {

  def ops: RDFOps[Bigdata]

  override def handleStatement(st: Statement): Unit = st.getObject match {
    case o: Literal if o.getDatatype == null && o.getLanguage == null =>
      super.handleStatement(
        new StatementImpl(
          st.getSubject,
          st.getPredicate,
          new LiteralImpl(o.getLabel, ops.xsd.string)))
    case other =>
      super.handleStatement(st)
  }

}

abstract class AbstractBigdataReader[T] extends RDFReader[Bigdata, Try, T] {

  implicit def ops: RDFOps[Bigdata]

  def getParser(): org.openrdf.rio.RDFParser

  def read(in: InputStream, base: String): Try[Bigdata#Graph] = Try {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractBigdataReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(in, base)
    new LinkedHashModel(triples)
  }

  def read(reader: Reader, base: String): Try[Bigdata#Graph] = Try {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractBigdataReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(reader, base)
    new LinkedHashModel(triples)
  }

}

class BigdataTurtleReader(implicit val ops: RDFOps[Bigdata]) extends AbstractBigdataReader[Turtle] {
  def getParser() = new org.openrdf.rio.turtle.TurtleParser
}

class BigdataRDFXMLReader(implicit val ops: RDFOps[Bigdata]) extends AbstractBigdataReader[RDFXML] {
  def getParser() = new org.openrdf.rio.rdfxml.RDFXMLParser
}

*/


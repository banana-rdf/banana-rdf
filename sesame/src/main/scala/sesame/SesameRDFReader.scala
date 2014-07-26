package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl.{ LinkedHashModel, StatementImpl, LiteralImpl }
import java.io._
import java.util.LinkedList
import scala.util._
import scala.concurrent.Future

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

  import Ops._

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

  override def read(is: String, base: String) =
    Future.fromTry(read(new ByteArrayInputStream(is.getBytes("UTF-8")),base))

}

class SesameRDFXMLReader(implicit Ops: SesameOps) extends RDFReader[Sesame, RDFXML] {

  import Ops._

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

  override def read(is: String, base: String) = {
      Future.fromTry(read(new ByteArrayInputStream(is.getBytes("UTF-8")),base))
  }
}

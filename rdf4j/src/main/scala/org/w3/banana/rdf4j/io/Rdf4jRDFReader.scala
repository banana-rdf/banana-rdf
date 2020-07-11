package org.w3.banana.rdf4j.io

import java.io._
import java.util.LinkedList

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl.{LinkedHashModel, SimpleValueFactory}
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser
import org.eclipse.rdf4j.rio.turtle.TurtleParser
import org.w3.banana._
import org.w3.banana.io._
import org.w3.banana.rdf4j.Rdf4j

import scala.util._

trait CollectorFix extends org.eclipse.rdf4j.rio.helpers.StatementCollector {

  def ops: RDFOps[Rdf4j]

  val valueFactory: ValueFactory = SimpleValueFactory.getInstance()

  override def handleStatement(st: Statement): Unit = st.getObject match {
    case o: Literal if o.getDatatype == null && o.getLanguage == null =>
      super.handleStatement(
        valueFactory.createStatement(
          st.getSubject,
          st.getPredicate,
          valueFactory.createLiteral(o.getLabel, ops.xsd.string)))
    case _ =>
      super.handleStatement(st)
  }

}

abstract class AbstractRdf4jReader[T] extends RDFReader[Rdf4j, Try, T] {

  implicit def ops: RDFOps[Rdf4j]

  def getParser(): org.eclipse.rdf4j.rio.RDFParser

  def read(in: InputStream, base: String): Try[Rdf4j#Graph] = Try {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.eclipse.rdf4j.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractRdf4jReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(in, base)
    new LinkedHashModel(triples)
  }

  def read(reader: Reader, base: String): Try[Rdf4j#Graph] = Try {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.eclipse.rdf4j.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractRdf4jReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(reader, base)
    new LinkedHashModel(triples)
  }

}

class Rdf4jTurtleReader(implicit val ops: RDFOps[Rdf4j]) extends AbstractRdf4jReader[Turtle] {
  def getParser(): TurtleParser = new TurtleParser()
}

class Rdf4jRDFXMLReader(implicit val ops: RDFOps[Rdf4j]) extends AbstractRdf4jReader[RDFXML] {
  def getParser(): RDFXMLParser = new RDFXMLParser()
}

class Rdf4jJSONLDReader(implicit val ops: RDFOps[Rdf4j]) extends AbstractRdf4jReader[JsonLd] {
  def getParser()  = new JSONLDParser()
}


package org.w3.banana.sesame.io

import java.io._
import java.util.LinkedList

import com.github.jsonldjava.sesame.SesameJSONLDParser
import org.openrdf.model._
import org.openrdf.model.impl.{ LinkedHashModel, LiteralImpl, StatementImpl }
import org.openrdf.rio.helpers.{ JSONLDMode, JSONLDSettings }
import org.w3.banana._
import org.w3.banana.sesame.Sesame
import org.w3.banana.io._
import scala.util._

trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {

  def ops: RDFOps[Sesame]

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

abstract class AbstractSesameReader[T] extends RDFReader[Sesame, Try, T] {

  implicit def ops: RDFOps[Sesame]

  def getParser(): org.openrdf.rio.RDFParser

  def read(in: InputStream, base: String): Try[Sesame#Graph] = Try {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractSesameReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(in, base)
    new LinkedHashModel(triples)
  }

  def read(reader: Reader, base: String): Try[Sesame#Graph] = Try {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractSesameReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(reader, base)
    new LinkedHashModel(triples)
  }

}

class SesameTurtleReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameReader[Turtle] {
  def getParser() = new org.openrdf.rio.turtle.TurtleParser
}

class SesameRDFXMLReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameReader[RDFXML] {
  def getParser() = new org.openrdf.rio.rdfxml.RDFXMLParser
}

/* Note: an issue with the com.github.jsonldjava is apparently that it
 * loads the whole JSON file into memory, which is memory consumptive
 */
abstract class AbstractSesameJSONLDReader[T](jsonldProfile: JSONLDMode) extends AbstractSesameReader[T] {

  def getParser() = {
    val parser = new SesameJSONLDParser
    parser.getParserConfig.set(JSONLDSettings.JSONLD_MODE, jsonldProfile)
    parser
  }

}

class SesameJSONLDCompactedReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameJSONLDReader[JsonLdCompacted](jsonldProfile = JSONLDMode.COMPACT)

class SesameJSONLDExpandedReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameJSONLDReader[JsonLdExpanded](jsonldProfile = JSONLDMode.EXPAND)

class SesameJSONLDFlattenedReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameJSONLDReader[JsonLdFlattened](jsonldProfile = JSONLDMode.FLATTEN)

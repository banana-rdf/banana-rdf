package org.w3.banana.sesame

import java.io._
import java.util.LinkedList

import com.github.jsonldjava.sesame.SesameJSONLDParser
import org.openrdf.model._
import org.openrdf.model.impl.{ LinkedHashModel, LiteralImpl, StatementImpl }
import org.openrdf.rio.helpers.{ JSONLDSettings, JSONLDMode }
import org.w3.banana._

import scala.util._

trait CollectorFix extends org.openrdf.rio.helpers.StatementCollector {

  override def handleStatement(st: Statement): Unit = st.getObject match {
    case o: Literal if o.getDatatype == null && o.getLanguage == null =>
      super.handleStatement(
        new StatementImpl(
          st.getSubject,
          st.getPredicate,
          new LiteralImpl(o.getLabel, Sesame.ops.__xsdString)))
    case other =>
      super.handleStatement(st)
  }

}

trait AbstractSesameReader[T <: RDFSerializationFormat] extends RDFReader[Sesame, T] {
  def getParser: org.openrdf.rio.RDFParser
  def read(in: InputStream, base: String): Try[Sesame#Graph] = Try {
    val parser = getParser
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix
    parser.setRDFHandler(collector)
    parser.parse(in, base)
    new LinkedHashModel(triples)
  }
}

object SesameTurtleReader extends AbstractSesameReader[Turtle] {
  val syntax = Syntax[Turtle]
  def getParser() = new org.openrdf.rio.turtle.TurtleParser
}
object SesameRDFXMLReader extends AbstractSesameReader[RDFXML] {
  val syntax = Syntax[RDFXML]
  def getParser() = new org.openrdf.rio.rdfxml.RDFXMLParser
}

/**
 * Note: an issue with the com.github.jsonldjava is apparently that it
 * loads the whole JSON file into memory, which is memory consumptive
 */
trait AbstractSesameJSONLDReader[T <: JsonLD] extends AbstractSesameReader[T] {
  def jsonldProfile: JSONLDMode
  def getParser() = {
    val parser = new SesameJSONLDParser
    parser.getParserConfig.set(JSONLDSettings.JSONLD_MODE, jsonldProfile)
    parser
  }
}

object SesameJSONLDCompactedReader extends AbstractSesameJSONLDReader[JsonLdCompacted] {
  val syntax = Syntax[JsonLdCompacted]
  def jsonldProfile = JSONLDMode.COMPACT
}
object SesameJSONLDExpandedReader extends AbstractSesameJSONLDReader[JsonLdExpanded] {
  val syntax = Syntax[JsonLdExpanded]
  def jsonldProfile = JSONLDMode.EXPAND
}
object SesameJSONLDFlattenedReader extends AbstractSesameJSONLDReader[JsonLdFlattened] {
  val syntax = Syntax[JsonLdFlattened]
  def jsonldProfile = JSONLDMode.FLATTEN
}


package org.w3.banana.sesame.io

import java.io._
import java.util.LinkedList

import com.github.jsonldjava.sesame.SesameJSONLDParser
import org.openrdf.model._
import org.openrdf.model.impl._

import scala.collection.JavaConverters._
import org.w3.banana._
import org.w3.banana.io._
import org.w3.banana.sesame.{Sesame, SesameUtil}

import scala.collection.mutable
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

abstract class AbstractQuadSesameReader[T] extends AbstractSesameReader[T] with RDFQuadReader[Sesame, Try, T] {

  implicit def ops: RDFOps[Sesame]

  def getParser(): org.openrdf.rio.RDFParser

  protected def getContextMap(triples: java.util.Collection[Statement]): Map[Option[Sesame#Node], Sesame#Graph] = {
    val graphMap = mutable.Map.empty[Option[Sesame#Node], Sesame#Graph]
    for(statement <- triples.asScala) {
      val context = Option(statement.getContext) match {
        case Some(uri: Sesame#URI) => Some(uri)
        case Some(uri: Sesame#BNode) => Some(uri)
        case _ => None
      }
      if(!graphMap.contains(context)) {
        val graph = new LinkedHashModel()
        graph.add(statement)
        graphMap += (context -> graph)
      } else {
        graphMap(context).add(statement)
      }
    }
    graphMap.toMap
  }

  def readAll(reader: Reader, base: String): Try[Map[Option[Sesame#Node], Sesame#Graph]] = Try {
    val triples = prepareParser(reader, base)
    getContextMap(triples)
  }

  def readAll(in: InputStream, base: String): Try[Map[Option[Sesame#Node], Sesame#Graph]] = Try {
    val triples = prepareParser(in, base)
    getContextMap(triples)
  }

  def read(reader: Reader, base: String, graphName: Sesame#URI): Try[Sesame#Graph] = Try {
    val triples = prepareParser(reader, base)
    new LinkedHashModel(triples).filter( null, null, null, graphName)
  }

  def read(in: InputStream, base: String, graphName: Sesame#URI): Try[Sesame#Graph]  = Try {
    val triples = prepareParser(in, base)
    new LinkedHashModel(triples).filter( null, null, null, graphName)
  }

  def readDefaultGraph(reader: Reader, base: String): Try[Sesame#Graph] = Try {
    val triples = prepareParser(reader, base)
    new LinkedHashModel(triples).filter( null, null, null, null )
  }

  def readDefaultGraph(in: InputStream, base: String): Try[Sesame#Graph]  = Try {
    val triples = prepareParser(in, base)
    new LinkedHashModel(triples).filter( null, null, null, null)
  }


}

abstract class AbstractSesameReader[T] extends RDFReader[Sesame, Try, T] {

  implicit def ops: RDFOps[Sesame]

  def getParser(): org.openrdf.rio.RDFParser

  protected def prepareParser(reader: Reader, base: String): java.util.Collection[Statement] = {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractSesameReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(reader, base)
    triples
  }

  protected def prepareParser(in: InputStream, base: String): java.util.Collection[Statement] = {
    val parser = getParser()
    val triples = new LinkedList[Statement]
    val collector = new org.openrdf.rio.helpers.StatementCollector(triples) with CollectorFix {
      val ops = AbstractSesameReader.this.ops
    }
    parser.setRDFHandler(collector)
    parser.parse(in, base)
    triples
  }

  def read(in: InputStream, base: String): Try[Sesame#Graph] = Try {
    val triples = prepareParser(in, base)
    new LinkedHashModel(triples)
  }

  def read(reader: Reader, base: String): Try[Sesame#Graph] = Try {
    val triples = prepareParser(reader, base)
    new LinkedHashModel(triples)
  }

}

class SesameTriGReader(implicit val ops: RDFOps[Sesame]) extends AbstractQuadSesameReader[TriG] {
  def getParser() = new org.openrdf.rio.trig.TriGParser
}

class SesameTurtleReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameReader[Turtle] {
  def getParser() = new org.openrdf.rio.turtle.TurtleParser
}

class SesameRDFXMLReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameReader[RDFXML] {
  def getParser() = new org.openrdf.rio.rdfxml.RDFXMLParser
}

/**
 * Note: an issue with the com.github.jsonldjava is apparently that it
 * loads the whole JSON file into memory, which is memory consumptive
 */
class SesameJSONLDReader(implicit val ops: RDFOps[Sesame]) extends AbstractSesameReader[JsonLd] {
  def getParser() = new SesameJSONLDParser
}


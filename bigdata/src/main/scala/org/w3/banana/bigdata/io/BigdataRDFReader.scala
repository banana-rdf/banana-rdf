package org.w3.banana.bigdata.io
import java.io._
import java.util.LinkedList

import com.bigdata.rdf.model
import com.bigdata.rdf.model._
import org.openrdf.model._
import org.openrdf.model.impl.{LinkedHashModel, LiteralImpl, StatementImpl}
import org.openrdf.rio.rdfxml.RDFXMLParser
import org.openrdf.rio.{ParseErrorListener, RDFHandler}
import org.w3.banana._
import org.w3.banana.bigdata.{BigdataMGraph, BigdataGraph, Bigdata}
import org.w3.banana.io._
import com.bigdata.rdf.rio.turtle
import scala.util._

class TurtleBigdataParser(base:String) extends com.bigdata.rdf.rio.turtle.BigdataTurtleParser{

  this.setValueFactory(BigdataValueFactoryImpl.getInstance(base))

}

class BigdataCollector(implicit ops:RDFOps[Bigdata]) extends RDFHandler with ParseErrorListener
{

  val graph: BigdataMGraph = ops.makeEmptyMGraph()

  def robustStatement(st:BigdataStatement):BigdataStatement = st.getObject match {
    case o:Literal if o.getDatatype == null && o.getLanguage == null =>
      val factory = o.getValueFactory
      val newObj = factory.createLiteral(o.getLabel, ops.xsd.string) //fix if literal is string with null datatype
      factory.createStatement(st.getSubject,st.getPredicate,newObj,st.getContext,st.getStatementType,st.getUserFlag)
    case _=>st //if everything is right, than return itself
  }


  override def handleStatement(st: Statement): Unit = st match {
    case bst:BigdataStatement=>
      ops.addTriple(graph,robustStatement(bst))
    case other=> println(s"ERROR: the statement ${other} of ${other.getClass.getName} class is of wrong type")
  }


  override def endRDF(): Unit = {
  }

  override def startRDF(): Unit = {
  }

  override def fatalError(msg: String, lineNo: Int, colNo: Int): Unit = {
    //what logger should we use in banana-rdf?
    //I do not like using print statements
    println(s"FATAL error $msg at LINE $lineNo COL $colNo occurred")
  }

  override def error(msg: String, lineNo: Int, colNo: Int): Unit = {
    println(s"nonfatal error $msg at LINE $lineNo COL $colNo occurred")

  }

  override def warning(msg: String, lineNo: Int, colNo: Int): Unit = {
    println(s"WARNING $msg at LINE $lineNo COL $colNo occurred")
  }

  override def handleComment(s: String): Unit = {

  }

  override def handleNamespace(s: String, s1: String): Unit = {
  }
}

abstract class AbstractBigdataReader[T] extends RDFReader[Bigdata, Try, T] {

  implicit def ops: RDFOps[Bigdata]


  def getParser(base:String): org.openrdf.rio.RDFParser

  def read(in: InputStream, base: String): Try[Bigdata#Graph] = Try {
    val parser = getParser(base)
    val collector =  new BigdataCollector
    parser.setRDFHandler(collector)
    parser.parse(in, base)
    collector.graph.graph
  }

  def read(reader: Reader, base: String): Try[Bigdata#Graph] = Try {
    val parser = getParser(base)
    val collector = new BigdataCollector
    parser.setRDFHandler(collector)
    parser.parse(reader, base)
    collector.graph.graph
  }

}

class BigdataTurtleReader(implicit val ops: RDFOps[Bigdata]) extends AbstractBigdataReader[Turtle] {
  override def getParser(base:String): org.openrdf.rio.RDFParser = new TurtleBigdataParser(base)

}


class RdfXmlBigdataParser(base:String) extends RDFXMLParser{

  this.setValueFactory(BigdataValueFactoryImpl.getInstance(base))

}

class BigdataRDFXMLReader(implicit val ops: RDFOps[Bigdata]) extends AbstractBigdataReader[RDFXML] {
  override def getParser(base:String): org.openrdf.rio.RDFParser = new RdfXmlBigdataParser(base)
}




package org.w3.banana.bigdata.io

import java.io._

import org.w3.banana.RDFOps
import org.w3.banana.bigdata.{Bigdata, BigdataOps}
import org.w3.banana.io._

import scala.util._

class BigdataRDFWriter[T](implicit
  ops: RDFOps[Bigdata],
  bigdataSyntax: BigdataSyntax[T]
) extends RDFWriter[Bigdata, Try, T] {

  def write(graph: Bigdata#Graph, os: OutputStream, base: String): Try[Unit] = Try {
    val sWriter = bigdataSyntax.rdfWriter(os, base)
    sWriter.startRDF()
    ops.getTriples(graph) foreach sWriter.handleStatement
    sWriter.endRDF()
  }

  def asString(graph: Bigdata#Graph, base: String): Try[String] = Try {
    val result = new StringWriter()
    val sWriter = bigdataSyntax.rdfWriter(result, base)
    sWriter.startRDF()
    ops.getTriples(graph) foreach sWriter.handleStatement
    sWriter.endRDF()
    result.toString
  }
}


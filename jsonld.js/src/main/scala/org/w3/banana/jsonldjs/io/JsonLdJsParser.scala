package org.w3.banana
package jsonldjs
package io

import org.w3.banana.io._
import java.io._
import scala.concurrent.{ Future, ExecutionContext }

/** A JSON-LD parser for jsonld.js. */
class JsonLdJsParser[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  ec: ExecutionContext
) extends RDFReader[Rdf, Future, JsonLd] {

  import ops._

  def read(reader: Reader, base: String): Future[Rdf#Graph] = {
    // shouldn't rely on BufferedReader but kinda lazy for now...
    val bf = new BufferedReader(reader)
    @annotation.tailrec
    def loop(sb: StringBuffer): String = {
      val line = bf.readLine()
      if (line == null) {
        bf.close()
        sb.toString()
      } else {
        sb.append(line)
        sb.append('\n')
        loop(sb)
      }
    }
    val input = loop(new StringBuffer())
    // TODO: why can't I dispatch on jsonld directly???
    //jsonld.toRDF(input, base)
    jsonldHelper.toRDF(input, base)
  }

  def read(is: InputStream, base: String): Future[Rdf#Graph] = {
    val reader = new InputStreamReader(is, "UTF-8")
    read(reader, base)
  }

}

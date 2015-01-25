package org.w3.banana
package n3js
package io

import n3js.js._
import org.w3.banana.io._
import java.io._
import scala.concurrent.{ Future, ExecutionContext }

class N3jsTurtleParser[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  ec: ExecutionContext
) extends RDFReader[Rdf, Future, Turtle] {

  import ops._

  def read(reader: Reader, base: String): Future[Rdf#Graph] = {
    val mgraph = makeEmptyMGraph()
    val parser = js.N3.Parser()
    val future = parser.parseChunks((t: Triple) => mgraph += js.Triple.toBananaTriple(t))
    val bf = new BufferedReader(reader)
    @annotation.tailrec
    def loop(): Unit = {
      val line = bf.readLine()
      if (line == null) {
        bf.close()
        parser.end()
      } else {
        parser.addChunk(line)
        loop()
      }
    }
    parser.addChunk(s"@base <$base>.\n")
    loop()
    future.onFailure { case t: Throwable => println("N3jsTurtleParser.read"); t.printStackTrace() }
    future.map(_ => mgraph.makeIGraph())
  }

  def read(is: InputStream, base: String): Future[Rdf#Graph] = {
    val reader = new InputStreamReader(is, "UTF-8")
    read(reader, base)
  }

}

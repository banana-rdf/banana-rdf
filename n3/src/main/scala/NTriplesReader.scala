/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3.nomo

import org.w3.rdf.n3.{NTriplesParser, Listener}
import org.w3.rdf.{RDF, TurtleReader => RDFTurtleReader}
import java.io._
import nomo.Accumulator


/**
 * A Traditional blocking Reader for the nomo based NTriples parser .
 *
 * @author bblfish
 * @created 29/02/2012
 */

class NTriplesReader[Rdf <: RDF, F, E, X](val parser: NTriplesParser[Rdf, F, E, X, Listener[Rdf]])
  extends RDFTurtleReader(parser.ops) {

  /**
   *
   * @param is an ASCII data containing input stream (UTF-8) may work too
   * @param base is ignored, as NTriples parsers don't need to know the base of a document, all statements
   *             being complete
   * @return the graph or an error
   */
  def read(is: InputStream, base: String): Either[Throwable, Rdf#Graph] = {
    read(new InputStreamReader(is, "UTF-8"), base) //currently NTriples only supports ascii, and so utf8 will work.
  }

  /**
   *
   * @param reader containing the data to be parsed
   * @param base is ignored, as NTriples parsers don't need to know the base of a document, all statements
   *             being complete
   * @return the graph or an error
   */
  def read(reader: Reader, base: String): Either[Throwable, Rdf#Graph] = {
    val buf = new Array[Char](1024)  //todo: how could one set the size of the buffer?
    import parser.P._
    try {
      var state: Pair[Parser[Unit], Accumulator[Char, X, Listener[Rdf]]] = (parser.nTriples, parser.P.annotator(new Listener(ops, None)))
      Iterator continually reader.read(buf) takeWhile (-1 !=) foreach  { read =>
        state = state._1.feedChunked(buf.slice(0,read), state._2, read)
      }
      val result = state._1.result(state._2)
      Right(ops.Graph(result.user.queue:_*))
    } catch {
      case e: IOException => Left(e)
    }

  }

}


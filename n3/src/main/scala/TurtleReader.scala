/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3.nomo

import org.w3.rdf.n3.{TurtleParser, Listener}
import org.w3.rdf.{RDF, TurtleReader => RDFTurtleReader}
import java.io._
import nomo.Accumulator


/**
 * A Traditional blocking Reader for the nomo based Turtle parser
 *
 * @author bblfish
 * @created 29/02/2012
 */

class TurtleReader[Rdf <: RDF, F, E, X](val parser: TurtleParser[Rdf, F, E, X, Listener[Rdf]])
  extends RDFTurtleReader(parser.ops) {

  /**
   *
   * @param is an input stream containing the data. There is no input stream detection mechanism,
   *           at present, so UTF-8 will be chosen.
   * @param base of the document being fetched. I.e. the URL of the document!
   * @return  the graph or an error
   */
  def read(is: InputStream, base: String): Either[Throwable, Rdf#Graph] = {
    read(new InputStreamReader(is, "UTF-8"), base)
  }

  /**
   *
   * @param reader containing the stream to be parsed
   * @param base  of the document being fetched. I.e. the URL of the document!
   * @return the graph or an error
   */
  def read(reader: Reader, base: String): Either[Throwable, Rdf#Graph] = {
    val buf = new Array[Char](1024)
    import parser.P._
    try {
      var state: Pair[Parser[Unit], Accumulator[Char, X, Listener[Rdf]]] =
        (parser.turtleDoc, parser.P.annotator(new Listener(ops, null)))

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


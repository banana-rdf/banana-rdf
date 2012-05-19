/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.banana.n3.nomo

import org.w3.banana.n3.{TurtleParser, Listener}
import org.w3.banana._
import java.io._
import nomo.Accumulator
import java.net.URI

import scalaz.Validation
import scalaz.Validation._

/**
 * A Traditional blocking Reader for the nomo based Turtle parser
 *
 * @author bblfish
 * @created 29/02/2012
 */

class TurtleReader[Rdf <: RDF, F, X](val parser: TurtleParser[Rdf, F, X, Listener[Rdf]])
  extends RDFReader[Rdf, Turtle] {

  // I don't know why, but this trick makes the presentation compiler happier
  type RdfGraph = Rdf#Graph

  import parser.ops
  
  /**
   *
   * @param is an input stream containing the data. There is no input stream detection mechanism,
   *           at present, so UTF-8 will be chosen.
   * @param base of the document being fetched. I.e. the URL of the document!
   * @return  the graph or an error
   */
  def read(is: InputStream, base: String): Validation[BananaException, RdfGraph] = {
    read(new InputStreamReader(is, "UTF-8"), base)
  }

  /**
   *
   * @param reader containing the stream to be parsed
   * @param base  of the document being fetched. I.e. the URL of the document!
   * @return the graph or an error
   */
  def read(reader: Reader, base: String): Validation[BananaException, Rdf#Graph] = WrappedThrowable.fromTryCatch {
    val buf = new Array[Char](4096)
    val abase = if (null != base  && "" !=base ) Some(new URI(base)) else None
    import parser.P._
    var state: Pair[Parser[Unit], Accumulator[Char, X, Listener[Rdf]]] =
      (parser.turtleDoc, parser.P.annotator(new Listener(ops, abase)))
    Iterator continually reader.read(buf) takeWhile (-1 !=) foreach  { read =>
      state = state._1.feedChunked(buf.slice(0,read), state._2, read)
    }
    val result = state._1.result(state._2)
    if (result.isSuccess) {
      ops.Graph(result.user.queue:_*)
    } else throw new Throwable(result.toString())  //todo, clearly this is not what we want
  }

}


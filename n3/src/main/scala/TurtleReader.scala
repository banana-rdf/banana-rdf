///*
// * Copyright (c) 2012 Henry Story
// * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
// */
//
//package org.w3.rdf.n3.nomo
//
//import org.w3.rdf.{RDF, RDFOperations, TurtleReader=>RDFTurtleReader}
//import sun.reflect.generics.reflectiveObjects.NotImplementedException
//import java.net.URI
//import java.io._
//import org.w3.rdf.n3.{Listener, TurtleParser}
//
//
///**
// * A Traditional Blocking Reader for the nomo based turtle parser
// *
// * @author bblfish
// * @created 29/02/2012
// */
//
//class TurtleReader[Rdf <: RDF, F, E, X](val parser: TurtleParser[Rdf, F, E, X, Listener[Rdf]])
//  extends RDFTurtleReader(parser.ops) {
//
//  /**
//   *
//   * @param is an ASCII data containing input stream (UTF-8) may work too
//   * @param base is null, NTriples parsers don't need to know the base of a document, all statements are complete
//   * @return  the graph or an error
//   */
//  def read(is: InputStream, base: String=null): Either[Throwable, Rdf#Graph] = {
//    read(new InputStreamReader(is,"UTF-8"),base) //currently NTriples only supports ascii, and so utf8 will work.
//  }
//
//  /**
//   *
//   * @param reader
//   * @param base
//   * @return the graph or an error
//   */
//  def read(reader: Reader, base: String=null): Either[Throwable, Rdf#Graph] = {
//    implicit def U = new Listener(ops, null)
//    var chunk = ParsedChunk(parser.turtleDoc, parser.P.annotator(U))
//    var inOpen = true
//    val in = new FileInputStream(testFile)
//    val bytes = new Array[Byte](randomSz)
//    info("setting Turtle parser to reading with chunk size of " + bytes.size + " bytes")
//
//    while (inOpen) {
//      try {
//        val length = in.read(bytes)
//        if (length > -1) {
//          chunk = chunk.parse(new String(bytes, 0, length, "ASCII"))
//        } else inOpen = false
//      } catch {
//        case e: IOException => inOpen = false
//      }
//    }
//    chunk.parser.result(chunk.acc)
//
//  }
//
//  case class ParsedChunk( val parser: Parser[Unit],
//                          val acc: Accumulator[Char, X, Listener[Rdf]] ) {
//    def parse(buf: Seq[Char]) = {
//      if (!buf.isEmpty) {
//        val (tripleParser, newAccu) = parser.feedChunked(buf, acc, buf.size)
//        ParsedChunk(tripleParser, newAccu)
//      } else {
//        this
//      }
//    }
//  }
//
//}

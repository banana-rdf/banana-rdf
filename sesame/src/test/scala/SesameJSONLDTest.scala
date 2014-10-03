package org.w3.banana.sesame
import org.w3.banana._
import Sesame._

class SesameJSONLDTest extends JSONLDTest[Sesame](Sesame.readerSelector, Sesame.writerSelector) {
  val turtleReader = RDFReader[Sesame, Turtle]
  val turtleWriter = RDFWriter[Sesame, Turtle]
  val jsonldReader = RDFReader[Sesame, JsonLdCompacted]
  val jsonldWriter = RDFWriter[Sesame, JsonLdCompacted]
}
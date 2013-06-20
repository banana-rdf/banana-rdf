package org.w3.banana.sesame

import org.w3.banana._
import Sesame._

class SesameJSONLDTest extends JSONLDTest[Sesame] {
  val turtleReader = RDFReader[Sesame, Turtle]
  val turtleWriter = RDFWriter[Sesame, Turtle]

  val jsonldReader = RDFReader[Sesame, JSONLD]
  val jsonldWriter = RDFWriter[Sesame, JSONLD]
}

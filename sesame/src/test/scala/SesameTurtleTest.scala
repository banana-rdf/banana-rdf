package org.w3.banana.sesame

import org.w3.banana._
import Sesame._

class SesameTurtleTest extends TurtleTestSuite[Sesame] {
  val reader = RDFReader[Sesame, Turtle]
  val writer = RDFWriter[Sesame, Turtle]
}

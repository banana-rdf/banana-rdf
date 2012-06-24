package org.w3.banana.sesame

import org.w3.banana
import banana.Turtle
import Sesame._
import SesameWriter._

class SesameTurtleTest extends banana.TurtleTestSuite[Sesame] {
  val reader = SesameTurtleReader
  val writer = SesameWriter[Turtle]
}

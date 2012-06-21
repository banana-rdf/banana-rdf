package org.w3.banana.sesame

import org.w3.banana
import banana.Turtle

class SesameTurtleTest extends banana.TurtleTestSuite(SesameOperations) {
  val reader = SesameTurtleReader
  val writer = SesameWriter[Turtle]
  val iso = SesameGraphIsomorphism
}

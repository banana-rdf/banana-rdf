package org.w3.banana.sesame

import org.w3.banana

class SesameTurtleTest extends banana.TurtleTestSuite(SesameOperations) {
  val reader = SesameTurtleReader
  val writer = SesameTurtleWriter
  val iso = SesameGraphIsomorphism
}

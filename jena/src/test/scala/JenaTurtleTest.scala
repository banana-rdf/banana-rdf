package org.w3.banana.jena

import org.w3.banana.TurtleTestSuite

class JenaTurtleTest extends TurtleTestSuite(JenaOperations) {
  val reader = JenaRDFReader.TurtleReader
  val writer = JenaTurtleWriter
  val iso = JenaGraphIsomorphism
}

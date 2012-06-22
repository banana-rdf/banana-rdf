package org.w3.banana.jena

import org.w3.banana.{Turtle, TurtleTestSuite}

class JenaTurtleTest extends TurtleTestSuite(JenaOperations) {
  val reader = JenaRDFReader.TurtleReader
  val writer = JenaRDFBlockingWriter[Turtle]
  val iso = JenaGraphIsomorphism
}

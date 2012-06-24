package org.w3.banana.jena

import org.w3.banana._
import Jena._
import JenaRDFBlockingWriter._

class JenaTurtleTest extends TurtleTestSuite[Jena] {
  val reader = JenaRDFReader.TurtleReader
  val writer = JenaRDFBlockingWriter[Turtle]
}

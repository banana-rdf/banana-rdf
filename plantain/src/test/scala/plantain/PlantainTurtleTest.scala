package org.w3.banana.plantain

import org.w3.banana._

class PlantainTurtleTest extends TurtleTestSuite[Plantain] {
  val reader = RDFReader[Plantain, Turtle]
  val writer = RDFWriter[Plantain, Turtle]
}

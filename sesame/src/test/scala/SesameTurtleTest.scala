package org.w3.rdf.sesame

import org.w3.rdf

class SesameTurtleTest extends rdf.TurtleTestSuite(SesameModule) {
  val reader = TurtleReader
  val iso = GraphIsomorphism
}
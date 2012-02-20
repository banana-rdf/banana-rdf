package org.w3.rdf.sesame

import org.w3.rdf

class SesameTurtleTest extends rdf.TurtleTestSuite(SesameModule) {
  val reader = SesameTurtleReader
  val writer = SesameTurtleWriter
  val iso = SesameGraphIsomorphism
}
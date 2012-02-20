package org.w3.rdf.jena

import org.w3.rdf.TurtleTestSuite

class JenaTurtleTest extends TurtleTestSuite(JenaModule) {
  val reader = JenaTurtleReader
  val iso = JenaGraphIsomorphism
}
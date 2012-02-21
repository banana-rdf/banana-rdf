package org.w3.rdf.jena

import org.w3.rdf.n3

class JenaNTriplesParserStringTest extends n3.ParserTest(JenaModule, JenaNTriplesStringParser) {
  val isomorphism = JenaGraphIsomorphism
  def toF(string: String) = string
}

class JenaNTriplesParserSeqTest extends n3.ParserTest(JenaModule, JenaNTriplesSeqParser) {
  val isomorphism = JenaGraphIsomorphism
  def toF(string: String) = string.toSeq
}

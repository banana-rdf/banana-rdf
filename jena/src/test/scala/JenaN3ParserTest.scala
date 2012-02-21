package org.w3.rdf.jena

import org.w3.rdf.n3

class JenaN3ParserStringTest extends n3.ParserTest(JenaModule, JenaN3StringParser) {
  val isomorphism = JenaGraphIsomorphism
  def toF(string: String) = string
}

class N3ParserSeqTest extends n3.ParserTest(JenaModule, JenaN3SeqParser) {
  val isomorphism = JenaGraphIsomorphism
  def toF(string: String) = string.toSeq
}

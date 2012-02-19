package org.w3.rdf.jena

import org.w3.rdf.n3

class N3ParserStringTest extends n3.ParserTest(JenaModule, N3StringParser) {
  val isomorphism = GraphIsomorphism
  
  def toF(string: String) = string
}


class N3ParserSeqTest extends n3.ParserTest(JenaModule, N3SeqParser) {
  val isomorphism = GraphIsomorphism
  
  def toF(string: String) = string.toSeq
}

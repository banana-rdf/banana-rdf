package org.w3.rdf.simple

import org.w3.rdf._
import org.w3.rdf.util.DefaultGraphIsomorphism

class SimpleModuleTest extends PimpsTest(SimpleModule)

class SimpleN3ParserSpec extends n3.NTriplesSpec(SimpleModule)

class N3ParserStringTest extends n3.ParserTest(SimpleModule, SimpleN3StringParser) {
  val isomorphism = SimpleGraphIsomorphism
  def toF(string: String) = string
}


class N3ParserSeqTest extends n3.ParserTest(SimpleModule, SimpleN3SeqParser) {
  val isomorphism = SimpleGraphIsomorphism
  def toF(string: String) = string.toSeq
}

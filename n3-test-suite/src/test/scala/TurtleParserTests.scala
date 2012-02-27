/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

import org.w3.rdf.n3
import org.w3.rdf.sesame
import org.w3.rdf.jena._
import sesame.SesameGraphIsomorphism

class JenaTurtleParserStringTest extends n3.TurtleParserTest(JenaOperations, JenaTurtleStringParser,JenaTurtleReader) {
  val morpheus = JenaGraphIsomorphism
}

class JenaTurtleParserSeqTest extends n3.TurtleParserTest(JenaOperations, JenaTurtleSeqParser,JenaTurtleReader) {
  val morpheus = JenaGraphIsomorphism
}

class SesameTurtleParserStringTest extends n3.TurtleParserTest(sesame.SesameOperations, sesame.TurtleStringParser,sesame.SesameTurtleReader) {
  val morpheus = SesameGraphIsomorphism
}

class SesameTurtleParserSeqTest extends n3.TurtleParserTest(sesame.SesameOperations, sesame.TurtleSeqParser,sesame.SesameTurtleReader) {
  val morpheus = SesameGraphIsomorphism
}


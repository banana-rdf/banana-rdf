/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

import org.w3.rdf.n3
import org.w3.rdf.sesame
import org.w3.rdf.jena
import sesame.SesameGraphIsomorphism

class JenaTurtleParserStringTest extends n3.TurtleParserTest(jena.JenaTurtleStringParser,jena.JenaTurtleReader) {
  val morpheus = jena.JenaGraphIsomorphism
}

class JenaTurtleParserSeqTest extends n3.TurtleParserTest(jena.JenaTurtleSeqParser,jena.JenaTurtleReader) {
  val morpheus = jena.JenaGraphIsomorphism
}

class SesameTurtleParserStringTest extends n3.TurtleParserTest(sesame.TurtleStringParser,sesame.SesameTurtleReader) {
  val morpheus = SesameGraphIsomorphism
}

class SesameTurtleParserSeqTest extends n3.TurtleParserTest(sesame.TurtleSeqParser,sesame.SesameTurtleReader) {
  val morpheus = SesameGraphIsomorphism
}


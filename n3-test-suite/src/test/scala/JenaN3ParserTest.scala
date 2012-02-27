/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.jena

import org.w3.rdf.n3

class JenaNTriplesParserStringTest extends n3.NTriplesParserTest(JenaOperations, JenaNTriplesStringParser) {
  val isomorphism = JenaGraphIsomorphism
  def toF(string: String) = string
}

class JenaNTriplesParserSeqTest extends n3.NTriplesParserTest(JenaOperations, JenaNTriplesSeqParser) {
  val isomorphism = JenaGraphIsomorphism
  def toF(string: String) = string.toSeq
}

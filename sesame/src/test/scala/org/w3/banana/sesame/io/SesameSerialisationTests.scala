package org.w3.banana.sesame.io

import org.w3.banana.io._
import org.w3.banana.sesame._
import scala.util.Try
import org.w3.banana.util.tryInstances._

class SesameTurtleTests extends TurtleTestSuite[Sesame, Try]

class SesameTriGTests extends TriGTestSuite[Sesame, Try]

class SesameTriGReaderTestSuite extends TriGReaderTestSuite[Sesame]

class SesameNTripleReaderTestSuite extends NTriplesReaderTestSuite[Sesame]

class SesameNTripleWriterTestSuite extends NTriplesWriterTestSuite[Sesame]

class SesameRdfXMLTests extends RdfXMLTestSuite[Sesame, Try]

class SesameJsonLDCompactedTests extends JsonLDTestSuite[Sesame, Try, JsonLdCompacted]

class SesameJsonLDExpandedTests extends JsonLDTestSuite[Sesame, Try, JsonLdExpanded]

class SesameJsonLDFlattened extends JsonLDTestSuite[Sesame, Try, JsonLdExpanded]

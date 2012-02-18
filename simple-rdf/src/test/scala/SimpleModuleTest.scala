package org.w3.rdf.simple

import org.w3.rdf._

class SimpleModuleTest extends PimpsTest(SimpleModule)

class SimpleN3ParserSpec extends n3.NTriplesSpec(SimpleModule)

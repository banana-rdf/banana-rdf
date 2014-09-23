package org.w3.banana.test

import jasmine.JasmineSjsTest

abstract class GraphUnionJasmineTest[Rdf <: RDF]()(implicit val ops: RDFOps[Rdf])
  extends JasmineSjsTest with GraphUnionBaseTest[Rdf]

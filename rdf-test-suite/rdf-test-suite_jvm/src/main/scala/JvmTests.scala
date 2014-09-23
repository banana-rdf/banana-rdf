package org.w3.banana.test

import jasmine.JasmineSpec

abstract GraphUnionJvmTest[Rdf <: RDF]()(implicit val ops: RDFOps[Rdf])
  extends JasmineSpec with GraphUnionBaseTest[Rdf]

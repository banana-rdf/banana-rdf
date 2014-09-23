package org.w3.banana

import com.github.inthenow.jasmine.scalatest.JasmineSpec

abstract class GraphUnionJvmTest[Rdf <: RDF]()(implicit val ops: RDFOps[Rdf])
  extends JasmineSpec with GraphUnionBaseTest[Rdf]

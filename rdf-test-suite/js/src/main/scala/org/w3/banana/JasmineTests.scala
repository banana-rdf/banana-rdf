package org.w3.banana

import com.github.inthenow.jasmine.sjs.JasmineSjsTest

abstract class GraphUnionJasmineTest[Rdf <: RDF]()(implicit val ops: RDFOps[Rdf])
  extends JasmineSjsTest with GraphUnionBaseTest[Rdf]

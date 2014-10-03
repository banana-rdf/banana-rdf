package org.w3.banana.rdfstorew.test


import org.w3.banana.jasmine.test._
import org.w3.banana.rdfstorew._


object PointedGraphJasmineTesterRDFStore extends PointedGraphJasmineTester[RDFStore]

object GraphUnionJasmineTest extends GraphUnionJasmineTest[RDFStore]

object DieselGraphConstructJasmineTest extends DieselGraphConstructJasmineTest[RDFStore]

object RDFStoreWDieselGraphExplorationJasmineTest extends DieselGraphExplorationJasmineTest[RDFStore]

object CommonBindersJasmineTest extends CommonBindersJasmineTest[RDFStore]

object RecordBinderJasmineTest extends RecordBinderJasmineTest[RDFStore]

object UriSyntaxJasmineTest extends UriSyntaxJasmineTest[RDFStore]

object TurtleTestJasmineSuite extends TurtleTestJasmineSuite[RDFStore]

object GraphStoreJasmineTest extends GraphStoreJasmineTest[RDFStore](RDFStoreW(Map()))

object SparqlEngineJasmineTest extends SparqlEngineJasmineTest[RDFStore](RDFStoreW(Map()))

object StandardIsomorphismTest extends IsomorphismTests[RDFStore]


package org.w3.banana.pome.test.jasmine


import org.w3.banana.jasmine.test._
import org.w3.banana.pome.Plantain



object PointedGraphJasmineTesterRDFStore extends PointedGraphJasmineTester[Plantain]

object GraphUnionJasmineTest extends GraphUnionJasmineTest[Plantain]

object DieselGraphConstructJasmineTest extends DieselGraphConstructJasmineTest[Plantain]

object RDFStoreWDieselGraphExplorationJasmineTest extends DieselGraphExplorationJasmineTest[Plantain]

object CommonBindersJasmineTest extends CommonBindersJasmineTest[Plantain]

object RecordBinderJasmineTest extends RecordBinderJasmineTest[Plantain]

object UriSyntaxJasmineTest extends UriSyntaxJasmineTest[Plantain]

//object TurtleTestJasmineSuite extends TurtleTestJasmineSuite[Plantain]

//object GraphStoreJasmineTest extends GraphStoreJasmineTest[RDFStore](new RDFStore)

//object SparqlEngineJasmineTest extends SparqlEngineJasmineTest[RDFStore](PlantainOps)

object StandardIsomorphismTest extends IsomorphismTests[Plantain]()


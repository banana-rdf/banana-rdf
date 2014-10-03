package org.w3.banana.plantain.test.jasmine

import org.w3.banana.GraphUnionJasmineTest
///import org.w3.banana.iso.{ IsomorphismTests, GraphIsomorphism, VerticeCBuilder, SimpleMappingGenerator }
import org.w3.banana.jasmine.test._
//import org.w3.banana.plantain.iso.GraphIsomorphismTest
import org.w3.banana.plantain.Plantain
///import org.w3.banana.rdf.iso.SimpleClassifyTest

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

//object StandardIsomorphismTest extends IsomorphismTests[Plantain]

/* Todo:
class PlantainSimpleClassifyTest() extends SimpleClassifyTest[Plantain](
  new SimpleMappingGenerator[Plantain](_))

class PlantainIsoGraphTest extends GraphIsomorphismTest[Plantain]((vtg: () => VerticeCBuilder[Plantain]) =>
  new GraphIsomorphism[Plantain](new SimpleMappingGenerator[Plantain](vtg)))


class PlantainIsoTest() extends IsomorphismTests[Plantain]

*/ 
package org.w3.banana.pome.test.jasmine

import org.w3.banana.iso.{ IsomorphismTests, GraphIsomorphism, VerticeCBuilder, SimpleMappingGenerator }
import org.w3.banana.jasmine.test._
import org.w3.banana.plantain.iso.GraphIsomorphismTest
import org.w3.banana.pome.Pome
import org.w3.banana.rdf.iso.SimpleClassifyTest

object PointedGraphJasmineTesterRDFStore extends PointedGraphJasmineTester[Pome]

object GraphUnionJasmineTest extends GraphUnionJasmineTest[Pome]

object DieselGraphConstructJasmineTest extends DieselGraphConstructJasmineTest[Pome]

object RDFStoreWDieselGraphExplorationJasmineTest extends DieselGraphExplorationJasmineTest[Pome]

object CommonBindersJasmineTest extends CommonBindersJasmineTest[Pome]

object RecordBinderJasmineTest extends RecordBinderJasmineTest[Pome]

object UriSyntaxJasmineTest extends UriSyntaxJasmineTest[Pome]

//object TurtleTestJasmineSuite extends TurtleTestJasmineSuite[Pome]

//object GraphStoreJasmineTest extends GraphStoreJasmineTest[RDFStore](new RDFStore)

//object SparqlEngineJasmineTest extends SparqlEngineJasmineTest[RDFStore](PomeOps)

//object StandardIsomorphismTest extends IsomorphismTests[Pome]

class PomeSimpleClassifyTest() extends SimpleClassifyTest[Pome](
  new SimpleMappingGenerator[Pome](_))

class PomeIsoGraphTest extends GraphIsomorphismTest[Pome]((vtg: () => VerticeCBuilder[Pome]) =>
  new GraphIsomorphism[Pome](new SimpleMappingGenerator[Pome](vtg)))

class PomeIsoTest() extends IsomorphismTests[Pome]

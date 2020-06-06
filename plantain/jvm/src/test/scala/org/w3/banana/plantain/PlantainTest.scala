package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.io._
import org.w3.banana.isomorphism._
import scala.util.Try
import org.w3.banana.util.tryInstances._

class PlantainTurtleTest extends TurtleTestSuite[Plantain, Try]

class PlantainNTripleReaderTestSuite extends NTriplesReaderTestSuite[Plantain]

class PlantainNTripleWriterTestSuite extends NTriplesWriterTestSuite[Plantain]

class PlantainSimpleClassifyTest() extends SimpleClassifyTest[Plantain](
  new SimpleMappingGenerator[Plantain](_))

class PlantainIsoGraphTest extends GraphIsomorphismTest[Plantain](
  (vtg: () => VerticeCBuilder[Plantain]) =>
  new GraphIsomorphism[Plantain](new SimpleMappingGenerator[Plantain](vtg))
)

/* this is testing that the implicits for diesel are correctly found */
object `this must compile` {

  import PlantainOps._

  val g: PointedGraph[Plantain] = (
    bnode("betehess") -- rdf.first ->- "Alexandre"
  )

  val gg: PointedGraph[Plantain] = (
    model.BNode("betehess") -- rdf.first ->- "Alexandre"
  )


}

// New shared tests

class PlantainOpsTest extends RDFOpsTest[Plantain]

class PlantainGraphTest extends GraphTest[Plantain]

class PlantainMGraphTest extends MGraphTest[Plantain]

class PlantainGraphUnionTest extends GraphUnionTest[Plantain]

class PlantainIsomorphismsTest extends IsomorphismTest[Plantain]

class PlantainPointedGraphTest extends PointedGraphTest[Plantain]

import org.w3.banana.diesel._

class PlantainDieselGraphConstructTest extends DieselGraphConstructTest[Plantain]

class PlantainDieselGraphExplorationTest extends DieselGraphExplorationTest[Plantain]

class PlantainDieselOwlPrimerTest extends DieselOwlPrimerTest[Plantain]

import org.w3.banana.binder._

class PlantainCommonBindersTest extends CommonBindersTest[Plantain]

class PlantainRecordBinderTest extends RecordBinderTest[Plantain]

class PlantainCustomBinderTest extends CustomBindersTest[Plantain]

import org.w3.banana.syntax._

class PlantainUriSyntaxTest extends UriSyntaxTest[Plantain]

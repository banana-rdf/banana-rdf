package org.w3.banana.plantain

import org.w3.banana._

//class PlantainTurtleTest extends TurtleTestSuite[Plantain]

object PlantainOpsTest extends RDFOpsTest[Plantain]

object PlantainGraphTest extends GraphTest[Plantain]

object PlantainMGraphTest extends MGraphTest[Plantain]

object PlantainGraphUnionTest extends GraphUnionTest[Plantain]

import org.w3.banana.isomorphism._

object PlantainIsomorphismsTest extends IsomorphismTest[Plantain]

object PlantainPointedGraphTest extends PointedGraphTest[Plantain]

import org.w3.banana.diesel._

object PlantainDieselGraphConstructTest extends DieselGraphConstructTest[Plantain]

object PlantainDieselGraphExplorationTest extends DieselGraphExplorationTest[Plantain]

object PlantainDieselOwlPrimerTest extends DieselOwlPrimerTest[Plantain]

import org.w3.banana.binder._

class PlantainCommonBindersTest extends CommonBindersTest[Plantain]

class PlantainRecordBinderTest extends RecordBinderTest[Plantain]

class PlantainCustomBinderTest extends CustomBindersTest[Plantain]

import org.w3.banana.syntax._

class PlantainUriSyntaxTest extends UriSyntaxTest[Plantain]

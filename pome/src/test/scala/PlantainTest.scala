package org.w3.banana.pome

import org.w3.banana._
import org.w3.banana.iso.OpsIsomorphismTests

//class PlantainTurtleTest extends TurtleTestSuite[Plantain]

class PlantainGraphUnionTest extends GraphUnionTest[Plantain]

class PlantainPointedGraphTest extends PointedGraphTester[Plantain]

import org.w3.banana.diesel._

class PlantainDieselGraphConstructTest extends DieselGraphConstructTest[Plantain]

class PlantainDieselGraphExplorationTest extends DieselGraphExplorationTest[Plantain]

import org.w3.banana.binder._

class PlantainCommonBindersTest extends CommonBindersTest[Plantain]

class PlantainRecordBinderTest extends RecordBinderTest[Plantain]

import org.w3.banana.syntax._

class PlantainUriSyntaxTest extends UriSyntaxTest[Plantain]

class PlantainStandardOpsIsomorphismTest() extends OpsIsomorphismTests[Plantain]



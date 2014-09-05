package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.plantain.util.GraphIsomorphismTest
import org.w3.banana.rdf.util.IsoSimpleClassifyTest
import org.w3.banana.util.{GraphIsomorphism, OpsIsomorphismTests, SimpleMappingGenerator}

class PlantainTurtleTest extends TurtleTestSuite[Plantain]

class PlantainGraphTest extends GraphTest[Plantain]

class PlantainPointedGraphTest extends PointedGraphTester[Plantain]

import org.w3.banana.diesel._

class PlantainDieselGraphConstructTest extends DieselGraphConstructTest[Plantain]

class PlantainDieselGraphExplorationTest extends DieselGraphExplorationTest[Plantain]

import org.w3.banana.binder._

class PlantainCommonBindersTest extends CommonBindersTest[Plantain]

class PlantainRecordBinderTest extends RecordBinderTest[Plantain]

import org.w3.banana.syntax._

class PlantainUriSyntaxTest extends UriSyntaxTest[Plantain]


class PlantainIsoSimpleClassifyTest() extends IsoSimpleClassifyTest[Plantain](
  new SimpleMappingGenerator[Plantain]())

class PlantainIsoGraphTest extends GraphIsomorphismTest[Plantain](new GraphIsomorphism[Plantain](new SimpleMappingGenerator))

class PlantainIsoOpsTest() extends OpsIsomorphismTests[Plantain]

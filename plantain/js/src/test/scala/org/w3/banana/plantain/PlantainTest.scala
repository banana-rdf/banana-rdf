
package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.isomorphism._

//
////class PlantainTurtleTest extends TurtleTestSuite[Plantain]

object PlantainGraphTest extends GraphTest[Plantain]

object PlantainMGraphTest extends MGraphTest[Plantain]

object PlantainGraphUnionTest extends GraphUnionTest[Plantain]

object PlantainIsomorphismsTest extends IsomorphismTest[Plantain]

object PlantainPointedGraphTest extends PointedGraphTest[Plantain]

import org.w3.banana.diesel._

object PlantainDieselGraphConstructTest extends DieselGraphConstructTest[Plantain]

object PlantainDieselGraphExplorationTest extends DieselGraphExplorationTest[Plantain]

import org.w3.banana.binder._

class PlantainCommonBindersTest extends CommonBindersTest[Plantain]

class PlantainRecordBinderTest extends RecordBinderTest[Plantain]

class PlantainCustomBinderTest extends CustomBindersTest[Plantain]

import org.w3.banana.syntax._

class PlantainUriSyntaxTest extends UriSyntaxTest[Plantain]

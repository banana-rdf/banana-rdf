package org.w3.banana
package n3js
import org.w3.banana.io._
import org.w3.banana.{ RDFOps, RDF }
import scala.concurrent.Future
import scalaz._
import Scalaz._
import scala.concurrent.ExecutionContext.Implicits.global
import N3js.ops
import org.w3.banana.util.TryInstances

object N3jsOpsTest extends RDFOpsTest[N3js]

object N3jsGraphTest extends GraphTest[N3js]

object N3jsMGraphTest extends MGraphTest[N3js]

object N3jsGraphUnionTest extends GraphUnionTest[N3js]

import org.w3.banana.isomorphism._

object N3jsIsomorphismsTest extends IsomorphismTest[N3js]

object N3jsPointedGraphTest extends PointedGraphTest[N3js]

import org.w3.banana.diesel._

object N3jsDieselGraphConstructTest extends DieselGraphConstructTest[N3js]

object N3jsDieselGraphExplorationTest extends DieselGraphExplorationTest[N3js]

import org.w3.banana.binder._

class N3jsCommonBindersTest extends CommonBindersTest[N3js]

class N3jsRecordBinderTest extends RecordBinderTest[N3js]

class N3jsCustomBinderTest extends CustomBindersTest[N3js]

//class N3jsTurtleSuite extends TurtleTestSuite[N3js, Future] //cannot use it as we do not have Comonad for Futures

import org.w3.banana.syntax._

// disabled because of https://github.com/scala-js/scala-js/issues/1521
// class N3jsUriSyntaxTest extends UriSyntaxTest[N3js]

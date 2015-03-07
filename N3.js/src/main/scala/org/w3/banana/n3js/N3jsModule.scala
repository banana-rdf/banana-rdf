package org.w3.banana
package n3js

import org.w3.banana.io._
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

trait N3jsModule
extends RDFModule
with RDFOpsModule
with RecordBinderModule {

  type Rdf = N3js

  implicit val ops: RDFOps[N3js] = N3jsOps

  implicit val recordBinder: binder.RecordBinder[N3js] = binder.RecordBinder[N3js]

  implicit val turtleReader: RDFReader[N3js, Future, Turtle] = new n3js.io.N3jsTurtleParser[N3js]

}

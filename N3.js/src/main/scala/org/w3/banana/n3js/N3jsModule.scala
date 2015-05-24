package org.w3.banana
package n3js

import org.w3.banana.io._
import org.w3.banana.n3js.io.N3jsTurtleWriter
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

trait N3jsModule
  extends RDFModule
  with RDFOpsModule
  with RecordBinderModule {

  type Rdf = N3js

  implicit val ops: RDFOps[Rdf] = N3jsOps

  implicit val recordBinder: binder.RecordBinder[Rdf] = binder.RecordBinder[N3js]

  implicit val turtleReader: RDFReader[Rdf, Future, Turtle] = new n3js.io.N3jsTurtleParser[N3js]

  implicit val turtleWriter: RDFWriter[Rdf, Future, Turtle] = new N3jsTurtleWriter

}

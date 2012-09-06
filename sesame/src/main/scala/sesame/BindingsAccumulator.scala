package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.query.{ TupleQueryResultHandler, BindingSet }

class BindingsAccumulator() extends TupleQueryResultHandler {

  private val builder = Vector.newBuilder[BindingSet]

  def endQueryResult(): Unit = ()

  def startQueryResult(bindingNames: java.util.List[String]): Unit = ()

  def handleSolution(bindingSet: BindingSet): Unit =
    builder += bindingSet

  def bindings(): Vector[BindingSet] = builder.result()

}

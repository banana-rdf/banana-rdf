package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.query.{ TupleQueryResultHandler, BindingSet }

class BindingsAccumulator() extends TupleQueryResultHandler {

  private val builder = Vector.newBuilder[BindingSet]

  def endQueryResult(): Unit = ()

  def startQueryResult(bindingNames: java.util.List[String]): Unit = ()

  def handleSolution(bindingSet: BindingSet): Unit =
    builder += bindingSet

  def handleBoolean(boolean: Boolean): Unit = new UnsupportedOperationException

  def handleLinks(linkUrls: java.util.List[String]): Unit = new UnsupportedOperationException

  def bindings(): Vector[BindingSet] = builder.result()

}

package org.w3.banana.rdfstorew

import org.w3.banana._
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js

class RDFStoreTurtleReader(implicit Ops: RDFStoreOps) extends RDFReader[RDFStore, Turtle] {

  import Ops._

  val syntax = Syntax[Turtle]

  def read(text: String, base: String): Future[RDFStore#Graph] = {
    val store:RDFStoreW = RDFStoreW(Map())
    store.load("text/turtle",text,base) flatMap {
      _ =>
        store.toGraph(base)
    }
  }

}

package org.w3.banana.rdfstorew

import org.w3.banana._
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import java.io.InputStream

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

  /** legacy: if one passes an input stream at this layer one
    * would need to know the encoding too. This function is badly designed.
    * @param is
    * @param base
    * @return
    */
  override def read(is: InputStream, base: String) = ???
}

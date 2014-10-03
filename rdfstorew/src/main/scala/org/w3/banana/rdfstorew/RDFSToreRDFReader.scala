package org.w3.banana.rdfstorew

import java.io.InputStream

import org.w3.banana._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

class RDFStoreTurtleReader(implicit ops: RDFStoreOps) extends RDFReader[RDFStore, Turtle] {

  val syntax = Syntax[Turtle]

  override def read(text: String, base: String): Future[RDFStore#Graph] = {
    val store: RDFStoreW = RDFStoreW(Map())
    store.load("text/turtle", text, base) flatMap {
      _ =>
        store.toGraph(base)
    }
  }

  /**
   * legacy: if one passes an input stream at this layer one
   * would need to know the encoding too. This function is badly designed.
   * @param is
   * @param base
   * @return
   */
  override def read(is: InputStream, base: String) = ???
}

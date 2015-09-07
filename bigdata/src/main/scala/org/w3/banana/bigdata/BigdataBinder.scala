package org.w3.banana.bigdata

import org.w3.banana.RDFOps
import org.w3.banana.binder.RecordBinder

class BigdataBinder(implicit ops: RDFOps[Bigdata]) extends RecordBinder[Bigdata]()(ops){

  import ops._
  private def s4(): String = Math.floor((1 + Math.random()) * 0x10000).toString().substring(1)

  /**
   * Create PGB with random UUID pointer.
   */
  override def pgb[T] = pgbWithId[T](_ => newUri(":"))

  override def newUri(prefix: String): Bigdata#URI = {
    val random:String = s4() + s4() + '-' + s4() + '-' + s4()
    //todo: add better random numbers generator
    if(prefix.endsWith(":") | prefix.endsWith("#") | prefix.endsWith("/")) URI(prefix+random) else URI(prefix+"/"+random)
  }
}

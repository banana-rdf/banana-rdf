package org.w3c.banana.rdfstorew

import scala.concurrent.{Promise, Await}
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.language.postfixOps

class RDFStore(store: js.Dynamic){

  def execute(sparql: String) : Any = {

    val promise = Promise[Any]

    store.applyDynamic("execute")(sparql, {(success:Boolean, res:Any) =>
      println("BACK FROM EXECUTE")
      println(success)
      println(res)
      if(success) {
        promise.success(res)
      } else {
        promise.failure(new Exception("Error running query: "+res))
      }
    })
    println("WAITING...")
    Await.result(promise.future, 10 seconds)
  }

  def load(mediaType:String, data:String, graph:String) : Boolean = {

    val promise = Promise[Boolean]

    store.applyDynamic("load")(mediaType, data, graph, {
      (success:Boolean, res:Any) =>
        println("BACK FROM LOAD")
        println(success)
        println(res)
        if(success) {
          promise.success(true)
        } else {
          promise.failure(new Exception("Error loading data into the sotre: "+res))
        }
    })
    println("WAITING...")
    Await.result(promise.future, 10 seconds)
  }

}

object RDFStore {

  def apply(options: Map[String,Any]): RDFStore = {

    val dic = options.foldLeft[js.Dictionary[Any]](js.Dictionary())({
      case (acc, (key, value)) =>
        acc.update(key,value); acc
    })

    val promise = Promise[RDFStore]


    global.rdfstore.applyDynamic("create")(dic, (store: js.Dynamic) => promise.success(new RDFStore(store)) )
    Await.result(promise.future, 10 seconds)
  }
}
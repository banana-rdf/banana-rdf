package org.w3.banana.rdfstorew

import scala.concurrent._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.language.postfixOps

class RDFStore(store: js.Dynamic){

  def execute(sparql: String) : Future[Any] = {

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

    promise.future
  }

  def load(mediaType:String, data:String, graph:String=null) : Future[Boolean] = {

    val promise = Promise[Boolean]

    val cb =  {
      (success:Boolean, res:Any) =>
        println("BACK FROM LOAD")
        println(success)
        println(res)
        if(success) {
          promise.success(true)
        } else {
          promise.failure(new Exception("Error loading data into the store: "+res))
        }
    }

    if(graph == null){
      store.applyDynamic("load")(mediaType,data,cb)
    } else {
      store.applyDynamic("load")(mediaType,data,graph,cb)
    }

    promise.future
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

    // always succeeds
    promise.future.value.get.get
  }
}
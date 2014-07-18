package org.w3.banana.rdfstorew

import scala.scalajs.js.JSApp
import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object TestApp extends JSApp with JSUtils {

  def main(): Unit = {

    /*


    // Building the store
    val options = Map("name" -> "hey")
    val store = RDFStoreW(options)
    println("FOUND STORE:")
    println(store)

    val data = "<http://test.com/something#me> <http://test.com/something/name> \"antonio\" ."
    val graph = "http://test.com/test_graph"


    store.load("text/n3",data).flatMap { _ =>
      store.execute("SELECT ?s ?p ?o WHERE { ?s ?p ?o }")
    } map { results =>
      println("RESULTS AT THE END")
      println(results)
    }

     */


    val ops = new RDFStoreOps()
    println("** building uri")
    val uri = ops.makeUri("http://test.com/something#mytype")
    println("** building literal")
    val literal = ops.makeLiteral("this is a test", uri)
    log("*** FINAL RESULT:")
    log(literal)
  }

}
package org.w3c.banana.rdfstorew

import scala.scalajs.js.JSApp
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object TestApp extends JSApp {

  def main(): Unit = {

    // Building the store
    val options = Map("name" -> "hey")
    val store = RDFStore(options)
    println("FOUND STORE:")
    println(store)

    val data = "<http://test.com/something#me> <http://test.com/something/name> \"antonio\" ."
    val graph = "http://test.com/test_graph"

    /*
    // Inserting data into the store
    if(store.load("text/n3",data, graph)) {
      println("Data loaded into the store")
    } else {
      println("Error loading data into the store")
    }


    // Running a query
    var results = store.execute("SELECT * WHERE { ?s ?p ?o }")
    println("EXECUTE RESULTS")
    println(results)
    */



    store.load("text/n3",data).flatMap { _ =>
      store.execute("SELECT ?s ?p ?o WHERE { ?s ?p ?o }")
    } map { results =>
      println("EXECUTE RESULTS")
      println(results)
    }
  }

}
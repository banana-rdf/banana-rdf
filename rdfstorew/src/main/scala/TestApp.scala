package org.w3.banana.rdfstorew

import scala.scalajs.js.{RegExp, Dynamic, JSApp}
import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import org.w3.banana._


object TestApp extends JSApp with JSUtils {

  import org.w3.banana.rdfstorew.RDFStore._
  import Ops._
  import org.w3.banana.diesel._

  val foaf = FOAFPrefix[Rdf]

  val betehess: PointedGraph[Rdf] = (
    toPointedGraphW[RDFStore](URI("http://bertails.org/#betehess")).a(foaf.Person)
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.name ->- "Alexander".lang("en")
      -- foaf.age ->- 29
      -- foaf("foo") ->- List(1, 2, 3)
      -- foaf.knows ->- (
    toPointedGraphW[RDFStore](URI("http://bblfish.net/#hjs")).a(foaf.Person)
        -- foaf.name ->- "Henry Story"
        -- foaf.currentProject ->- URI("http://webid.info/")
      )
    )

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


    /*
    println("** building uri")
    val uri = RDFStoreOps.makeUri("http://test.com/something#mytype")
    println("** building literal")
    val literal = RDFStoreOps.makeLiteral("this is a test", uri)
    log("*** THE LITERAL")
    log(literal)
    println(literal)
    println("datatype")
    println(literal.datatype)
    println("language")
    println(literal.language)

    val triple = RDFStoreOps.makeTriple(
      RDFStoreOps.makeUri("http://test.com/me"),
      RDFStoreOps.makeUri("foaf:name"),
      RDFStoreOps.makeLiteral("Antonio",null)
    )
    log("** THE TRIPLE")
    log(triple)
    println(triple)
*/



    println("HEY")
    println((betehess / foaf.age).as[Int])
  }

}
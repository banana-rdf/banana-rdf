package org.w3.banana.binder

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana._

class CustomBindersTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._
  
  "serializing and deserializing js.Date" in {
    import org.w3.banana.binder.JsDateBinders._

    import scala.scalajs.js

    val date = new js.Date(1989, 11, 9)
    //Javascript has no compare operator for dates so one must compare strings/ms etc
    date.toPG.as[js.Date].get.toDateString shouldEqual date.toDateString
    date.toPG.as[js.Date].get.getMilliseconds() shouldEqual date.getMilliseconds()
  }

}

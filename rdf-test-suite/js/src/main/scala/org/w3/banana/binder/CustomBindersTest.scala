package org.w3.banana.binder

import org.w3.banana._, syntax._, diesel._
import scala.util._
import zcheck.SpecLite

class CustomBindersTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

  import ops._
  
  "serializing and deserializing js.Date" in {
    import scala.scalajs.js
    import org.w3.banana.binder.JsDateBinders._

    val date = new js.Date(1989, 11, 9)
    date.toPG.as[js.Date] must_==(Success(date))
  }

}

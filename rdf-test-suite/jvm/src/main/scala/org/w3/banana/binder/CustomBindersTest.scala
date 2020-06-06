package org.w3.banana.binder

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import org.w3.banana._

class CustomBindersTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends AnyWordSpec with Matchers {

  import ops._
  
  "serializing and deserializing Joda DateTime" in {
    import org.joda.time.DateTime
    import org.w3.banana.binder.JodaTimeBinders._

    val dateTime = DateTime.now()
    dateTime.toPG.as[DateTime].get.compareTo(dateTime) shouldEqual 0
  }

}

package org.w3.banana.binder

import org.w3.banana._
import syntax._
import diesel._
import org.scalatest.WordSpec

import scala.util._

class CustomBindersTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec {

  import ops._
  
  "serializing and deserializing Joda DateTime" in {
    import org.joda.time.DateTime
    import org.w3.banana.binder.JodaTimeBinders._

    val dateTime = DateTime.now()
    assert(dateTime.toPG.as[DateTime].get.compareTo(dateTime) === (0))
  }

}

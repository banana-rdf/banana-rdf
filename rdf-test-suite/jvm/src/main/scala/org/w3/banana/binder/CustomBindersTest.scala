package org.w3.banana.binder

import org.w3.banana._, syntax._, diesel._
import scala.util._
import zcheck.SpecLite

class CustomBindersTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

  import ops._
  
  "serializing and deserializing Joda DateTime" in {
    import org.joda.time.DateTime
    import org.w3.banana.binder.JodaTimeBinders._

    val dateTime = DateTime.now()
    dateTime.toPG.as[DateTime].get.compareTo(dateTime) must_==(0)
  }

}

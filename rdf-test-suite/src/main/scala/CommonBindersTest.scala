package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import org.joda.time.DateTime
import scalaz._

abstract class CommonBindersTest[Rdf <: RDF](diesel: Diesel[Rdf])
extends WordSpec with MustMatchers {
  
  import diesel._
  import ops._

  "serializing and deserialiazing Joda DateTime" in {
    import DateTimeBinder._
    val dateTime = DateTime.now()
    fromNode(toNode(dateTime)).getOrElse(sys.error("problem")).compareTo(dateTime) must be (0)
  }

}

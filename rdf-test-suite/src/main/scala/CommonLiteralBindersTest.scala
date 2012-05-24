package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import org.joda.time.DateTime
import scalaz._

abstract class CommonLiteralBindersTest[Rdf <: RDF](ops: RDFOperations[Rdf])
extends WordSpec with MustMatchers {
  
  val commonLiteralBinders = CommonLiteralBinders(ops)
  import ops._
  import commonLiteralBinders._

  "serializing and deserialiazing Joda DateTime" in {
    import DateTimeBinder._
    val dateTime = DateTime.now()
    fromLiteral(toLiteral(dateTime)).getOrElse(sys.error("problem")).compareTo(dateTime) must be (0)
  }

}

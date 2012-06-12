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

  "serializing and deserializing a List" in {
    val binder = implicitly[PointedGraphBinder[Rdf, List[Int]]]
    val list = List(1, 2, 3)
    binder.fromPointedGraph(binder.toPointedGraph(list)) must be === (Success(list))
  }

  "serializing and deserializing a Tuple2" in {
    val binder = implicitly[PointedGraphBinder[Rdf, (Int, String)]]
    val tuple = (42, "42")
    binder.fromPointedGraph(binder.toPointedGraph(tuple)) must be === (Success(tuple))
  }

}

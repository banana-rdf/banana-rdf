package org.w3.banana.binder

import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers.MustMatchers
import org.joda.time.DateTime
import scala.util._

abstract class CommonBindersTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with MustMatchers {

  import ops._

  "serializing and deserialiazing Joda DateTime" in {
    val dateTime = DateTime.now()
    dateTime.toPG.as[DateTime].get.compareTo(dateTime) must be(0)
  }

  "serializing and deserialiazing a Boolean" in {
    true.toPG.as[Boolean] must be(Success(true))
    false.toPG.as[Boolean] must be(Success(false))
  }

  "serializing and deserializing an Integer" in {
    123.toPG.as[Int] must be(Success(123))
  }

  "serializing and deserializing a List of simple nodes" in {
    val binder = PGBinder[Rdf, List[Int]]
    val list = List(1, 2, 3)
    binder.fromPG(binder.toPG(list)) must be(Success(list))
  }

  "serializing and deserializing a List of complex types" in {
    val binder = implicitly[PGBinder[Rdf, List[List[Int]]]]
    val list = List(List(1, 2), List(3))
    binder.fromPG(binder.toPG(list)) must be(Success(list))
  }

  "serializing and deserializing a Tuple2" in {
    val binder = PGBinder[Rdf, (Int, String)]
    val tuple = (42, "42")
    binder.fromPG(binder.toPG(tuple)) must be(Success(tuple))
  }

  "serializing and deserializing a Map" in {
    val binder = PGBinder[Rdf, Map[String, List[Int]]]
    val map = Map("1" -> List(1, 2, 3), "2" -> List(4, 5))
    binder.fromPG(binder.toPG(map)) must be(Success(map))
    binder.fromPG(binder.toPG(Map.empty)) must be(Success(Map.empty))
  }

  "serializing and deserializing an Either" in {
    val binder = PGBinder[Rdf, Either[String, List[Int]]]
    val StringPGBinder = PGBinder[Rdf, String]
    val left = Left("foo")
    val right = Right(List(1, 2, 3))
    binder.fromPG(binder.toPG(left)) must be(Success(left))
    binder.fromPG(binder.toPG(right)) must be(Success(right))
    binder.fromPG(StringPGBinder.toPG("foo")) must be('failure)
  }

  "serializing and deserialiazing Option" in {
    val opts: Option[String] = Some("foo")
    implicit val binder = PGBinder[Rdf, Option[String]]
    opts.toPG.as[Option[String]] must equal(Success(opts))
    (None: Option[String]).toPG.as[Option[String]] must equal(Success(None))
  }

  "the implicit chains must be complete" in {
    implicitly[PGBinder[Rdf, Rdf#URI]]
    implicitly[NodeBinder[Rdf, Rdf#URI]]
    implicitly[PGBinder[Rdf, Rdf#Node]]
    implicitly[ToURI[Rdf, Rdf#URI]]
    implicitly[FromURI[Rdf, Rdf#URI]]
  }

}

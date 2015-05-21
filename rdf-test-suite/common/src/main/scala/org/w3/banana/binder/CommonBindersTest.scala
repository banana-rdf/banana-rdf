package org.w3.banana.binder

import com.inthenow.zcheck.SpecLite
import org.w3.banana._, syntax._, diesel._
import scala.util._

class CommonBindersTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

  import ops._

  "serializing and deserialiazing a Boolean" in {
    val truePg = true.toPG
    truePg.pointer must_==(Literal("true", xsd.boolean))
    truePg.graph must_==(Graph.empty)
    true.toPG.as[Boolean] must_==(Success(true))

    val falsePg = false.toPG
    truePg.pointer must_==(Literal("true", xsd.boolean))
    truePg.graph must_==(Graph.empty)
    false.toPG.as[Boolean] must_==(Success(false))

  }

  "serializing and deserializing an Integer" in {
    val pg123 = 123.toPG
    pg123.pointer must_==(Literal("123", xsd.integer))
    pg123.graph must_==(Graph.empty)
    pg123.toPG.as[Int] must_==(Success(123))
  }

  "serializing and deserializing a List of simple nodes" in {
    val bn1 = BNode()
    val bn2 = BNode()
    val bn3 = BNode()
    val constructedListGr = Graph(
      Triple(bn1, rdf.first, Literal("1", xsd.integer)),
      Triple(bn1, rdf.rest, bn2),
      Triple(bn2, rdf.first, Literal("2", xsd.integer)),
      Triple(bn2, rdf.rest, bn3),
      Triple(bn3, rdf.first, Literal("3", xsd.integer)),
      Triple(bn3, rdf.rest, rdf.nil)
    )
    val binder = PGBinder[Rdf, List[Int]]
    val list = List(1, 2, 3)
    val listPg = binder.toPG(list)
    assert(listPg.graph isIsomorphicWith (constructedListGr))
    binder.fromPG(listPg) must_==(Success(list))
  }

  "serializing and deserializing a List of complex types" in {
    val binder = implicitly[PGBinder[Rdf, List[List[Int]]]]
    val list = List(List(1, 2), List(3))
    binder.fromPG(binder.toPG(list)) must_==(Success(list))
  }

  "serializing and deserializing a Tuple2" in {
    val binder = PGBinder[Rdf, (Int, String)]
    val tuple = (42, "42")
    binder.fromPG(binder.toPG(tuple)) must_==(Success(tuple))
  }

  "serializing and deserializing a Map" in {
    val binder = PGBinder[Rdf, Map[String, List[Int]]]
    val map = Map("1" -> List(1, 2, 3), "2" -> List(4, 5))
    binder.fromPG(binder.toPG(map)) must_==(Success(map))
    binder.fromPG(binder.toPG(Map.empty)) must_==(Success(Map.empty))
  }

  "serializing and deserializing an Either" in {
    val binder = PGBinder[Rdf, Either[String, List[Int]]]
    val StringPGBinder = PGBinder[Rdf, String]
    val left = Left("foo")
    val right = Right(List(1, 2, 3))
    binder.fromPG(binder.toPG(left)) must_==(Success(left))
    binder.fromPG(binder.toPG(right)) must_==(Success(right))
    check(binder.fromPG(StringPGBinder.toPG("foo")).isFailure)
  }

  "serializing and deserialiazing Option" in {
    val opts: Option[String] = Some("foo")
    implicit val binder = PGBinder[Rdf, Option[String]]
    opts.toPG.as[Option[String]] must_==(Success(opts))
    (None: Option[String]).toPG.as[Option[String]] must_==(Success(None))
  }

  def `this must compile` {
    implicitly[PGBinder[Rdf, Rdf#URI]]
    implicitly[NodeBinder[Rdf, Rdf#URI]]
    implicitly[PGBinder[Rdf, Rdf#Node]]
    implicitly[ToURI[Rdf, Rdf#URI]]
    implicitly[FromURI[Rdf, Rdf#URI]]
  }

}
